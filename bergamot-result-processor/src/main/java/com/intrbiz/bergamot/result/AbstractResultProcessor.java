package com.intrbiz.bergamot.result;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.model.message.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.update.LocationUpdate;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ActiveResultKey;
import com.intrbiz.bergamot.queue.key.AdhocResultKey;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.bergamot.queue.key.PassiveResultKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.queue.key.SchedulerKey;
import com.intrbiz.bergamot.queue.key.UpdateKey;
import com.intrbiz.bergamot.queue.key.UpdateKey.UpdateType;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.NullKey;

public abstract class AbstractResultProcessor implements ResultProcessor
{
    private Logger logger = Logger.getLogger(AbstractResultProcessor.class);
    
    private UUID instanceId = UUID.randomUUID();

    private WorkerQueue workerQueue;

    private List<Consumer<ResultMO, ResultKey>> resultConsumers = new LinkedList<Consumer<ResultMO, ResultKey>>();
    
    private List<Consumer<ResultMO, ResultKey>> fallbackConsumers = new LinkedList<Consumer<ResultMO, ResultKey>>();

    private List<Consumer<ExecuteCheck, NullKey>> deadConsumers = new LinkedList<Consumer<ExecuteCheck, NullKey>>();
    
    private List<Consumer<ExecuteCheck, NullKey>> deadAgentConsumers = new LinkedList<Consumer<ExecuteCheck, NullKey>>();

    private SchedulerQueue schedulerQueue;

    private RoutedProducer<SchedulerAction, SchedulerKey> schedulerActionProducer;

    private NotificationQueue notificationsQueue;

    private RoutedProducer<Notification, NotificationKey> notificationsProducer;

    private UpdateQueue updateQueue;

    private RoutedProducer<Update, UpdateKey> updateProducer;
    
    private RoutedProducer<ResultMO, AdhocResultKey> adhocProducer;

    private int threads = Runtime.getRuntime().availableProcessors();

    public AbstractResultProcessor()
    {
        super();
    }

    @Override
    public int getThreads()
    {
        return threads;
    }

    @Override
    public void setThreads(int threads)
    {
        this.threads = threads;
    }

    @Override
    public void ownPool(UUID site, int pool)
    {
        /*
         * Own a pool by adding bindings to our queue 
         */
        for (Consumer<ResultMO, ResultKey> consumer : this.resultConsumers)
        {
            consumer.addBinding(new ActiveResultKey(site, pool));
            consumer.addBinding(new PassiveResultKey(site));
            // Note: we only need to update the queue once, so break
            break;
        }
    }

    @Override
    public void disownPool(UUID site, int pool)
    {
        /*
         * Disown a pool by removing bindings from our queue
         */
        for (Consumer<ResultMO, ResultKey> consumer : this.resultConsumers)
        {
            consumer.removeBinding(new ActiveResultKey(site, pool));
            consumer.removeBinding(new PassiveResultKey(site));
            // Note: we only need to update the queue once, so break
            break;
        }
    }

    @Override
    public void start()
    {
        // setup the consumer
        logger.info("Creating result consumer");
        this.workerQueue = WorkerQueue.open();
        // create the consumers
        for (int i = 0; i < this.getThreads(); i++)
        {
            // consume results, currently for all sites
            this.resultConsumers.add(this.workerQueue.consumeResults((h, r) -> {
                if (logger.isTraceEnabled()) logger.trace("Processing pooled/site result: " + r);
                processExecuted(r);
            }, this.instanceId.toString()));
            // consume results, currently for all sites
            this.fallbackConsumers.add(this.workerQueue.consumeFallbackResults((h, r) -> {
                if (logger.isDebugEnabled()) logger.debug("Processing fallback result: " + r);
                processExecuted(r);
            }));
            // consume dead checks, currently for all sites
            this.deadConsumers.add(this.workerQueue.consumeDeadChecks((h, e) -> {
                processDead(e);
            }));
            // consume dead agent checks, currently for all sites
            this.deadAgentConsumers.add(this.workerQueue.consumeDeadAgentChecks((h, e) -> {
                processDeadAgent(e);
            }));
        }
        // scheduler queue
        this.schedulerQueue = SchedulerQueue.open();
        this.schedulerActionProducer = this.schedulerQueue.publishSchedulerActions();
        // notifications queue
        this.notificationsQueue = NotificationQueue.open();
        this.notificationsProducer = this.notificationsQueue.publishNotifications();
        // updates
        this.updateQueue = UpdateQueue.open();
        this.updateProducer = this.updateQueue.publishUpdates();
        // adhoc
        this.adhocProducer = this.workerQueue.publishAdhocResults();
    }
    
    protected void publishAdhocResult(ResultMO result)
    {
        if (logger.isTraceEnabled()) logger.trace("Publishing adhoc result to " + result.getAdhocId() + " with message " + result);
        this.adhocProducer.publish(new AdhocResultKey(result.getAdhocId()), result);
    }

    protected void rescheduleCheck(ActiveCheck<?, ?> check, long interval)
    {
        if (logger.isTraceEnabled()) logger.trace("Rescheduling " + check.getType() + "::" + check.getId() + " [" + check.getName() + "]" + " with new interval " + interval + " due to state change");
        this.schedulerActionProducer.publish(new SchedulerKey(check.getSiteId(), check.getPool()), new RescheduleCheck(check.getId(), interval));
    }

    protected void publishNotification(Check<?, ?> check, Notification notification)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending notification:\r\n" + notification);
        this.notificationsProducer.publish(new NotificationKey(check.getSiteId()), notification);
    }
    
    protected void publishAlertUpdate(Alert alert, AlertUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending alert update:\r\n" + update);
        this.updateProducer.publish(new UpdateKey(UpdateType.ALERT, alert.getSiteId(), alert.getId()), update);
    }

    protected void publishCheckUpdate(Check<?, ?> check, CheckUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending check update:\r\n" + update);
        this.updateProducer.publish(new UpdateKey(UpdateType.CHECK, check.getSiteId(), check.getId()), update);
    }
    
    protected void publishGroupUpdate(Group group, GroupUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending group update:\r\n" + update);
        this.updateProducer.publish(new UpdateKey(UpdateType.GROUP, group.getSiteId(), group.getId()), update);
    }
    
    protected void publishLocationUpdate(Location location, LocationUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending location update:\r\n" + update);
        this.updateProducer.publish(new UpdateKey(UpdateType.LOCATION, location.getSiteId(), location.getId()), update);
    }
}

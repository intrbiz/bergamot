package com.intrbiz.bergamot.result;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;

public abstract class AbstractResultProcessor implements ResultProcessor
{
    private Logger logger = Logger.getLogger(AbstractResultProcessor.class);
    
    private UUID instanceId = UUID.randomUUID();

    private WorkerQueue workerQueue;

    private List<Consumer<Result>> resultConsumers = new LinkedList<Consumer<Result>>();
    
    private List<Consumer<Result>> fallbackConsumers = new LinkedList<Consumer<Result>>();

    private List<Consumer<ExecuteCheck>> deadConsumers = new LinkedList<Consumer<ExecuteCheck>>();

    private SchedulerQueue schedulerQueue;

    private RoutedProducer<SchedulerAction> schedulerActionProducer;

    private NotificationQueue notificationsQueue;

    private RoutedProducer<Notification> notificationsProducer;

    private UpdateQueue updateQueue;

    private RoutedProducer<Update> updateProducer;

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
        for (Consumer<Result> consumer : this.resultConsumers)
        {
            consumer.addBinding(new ResultKey(site, pool));
            break;
        }
        for (Consumer<Result> consumer : this.fallbackConsumers)
        {
            consumer.addBinding(new ResultKey(site, pool));
            break;
        }
        for (Consumer<ExecuteCheck> consumer : this.deadConsumers)
        {
            consumer.addBinding(new ResultKey(site, pool));
            break;
        }
    }

    @Override
    public void disownPool(UUID site, int pool)
    {
        for (Consumer<Result> consumer : this.resultConsumers)
        {
            consumer.removeBinding(new ResultKey(site, pool));
            break;
        }
        for (Consumer<Result> consumer : this.fallbackConsumers)
        {
            consumer.removeBinding(new ResultKey(site, pool));
            break;
        }
        for (Consumer<ExecuteCheck> consumer : this.deadConsumers)
        {
            consumer.removeBinding(new ResultKey(site, pool));
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
            this.resultConsumers.add(this.workerQueue.consumeResults((r) -> {
                logger.trace("Processing pooled result");
                processExecuted(r);
            }, this.instanceId.toString()));
            // consume results, currently for all sites
            this.fallbackConsumers.add(this.workerQueue.consumeFallbackResults((r) -> {
                logger.trace("Processing fallback result");
                processExecuted(r);
            }));
            // consume dead checks, currently for all sites
            this.deadConsumers.add(this.workerQueue.consumeDeadChecks((e) -> {
                processDead(e);
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
    }

    protected void rescheduleCheck(ActiveCheck<?, ?> check)
    {
        if (logger.isTraceEnabled()) logger.trace("Rescheduling " + check + " due to state change");
        this.schedulerActionProducer.publish(new GenericKey(check.getSiteId().toString()), new RescheduleCheck(check.getId()));
    }

    protected void publishNotification(Check<?, ?> check, Notification notification)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending notification:\r\n" + notification);
        this.notificationsProducer.publish(new GenericKey(check.getSiteId().toString()), notification);
    }

    protected void publishUpdate(Check<?, ?> check, Update update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending update:\r\n" + update);
        this.updateProducer.publish(new GenericKey(check.getSiteId().toString() + "." + check.getId()), update);
    }
}

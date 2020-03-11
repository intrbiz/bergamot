package com.intrbiz.bergamot.result;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.queue.NotificationProducer;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolConsumer;
import com.intrbiz.bergamot.cluster.queue.SchedulerActionProducer;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.update.LocationUpdate;

public abstract class AbstractResultProcessor implements ResultProcessor
{
    private static final Logger logger = Logger.getLogger(AbstractResultProcessor.class);
    
    protected final UUID poolId;
    
    protected final ProcessingPoolConsumer consumer;
    
    protected final SchedulerActionProducer schedulerActions;
    
    protected final SiteNotificationTopic notificationBroker;
    
    protected final NotificationProducer notificationProducer;
    
    protected final SiteUpdateTopic updateBroker;

    protected int threadCount;
    
    protected Thread[] threads;
    
    protected volatile boolean run = false;

    public AbstractResultProcessor(
        UUID poolId, 
        ProcessingPoolConsumer consumer,
        SchedulerActionProducer schedulerActions,
        NotificationProducer notificationProducer,
        SiteNotificationTopic notificationBroker,
        SiteUpdateTopic updateBroker
    ) {
        super();
        this.poolId = poolId;
        this.consumer = consumer;
        this.schedulerActions = schedulerActions;
        this.notificationProducer = notificationProducer;
        this.notificationBroker = notificationBroker;
        this.updateBroker = updateBroker;
        this.threadCount = Runtime.getRuntime().availableProcessors();
    }
    
    public UUID getPoolId()
    {
        return this.poolId;
    }

    @Override
    public void start()
    {
        logger.info("Starting result processor");
        // Start the executor
        this.startExecutors();
    }
    
    protected void startExecutors()
    {
        this.run = true;
        this.threads = new Thread[this.threadCount];
        for (int i = 0; i < this.threads.length; i++)
        {
            final int threadNum = i;
            this.threads[i] = new Thread(() -> {
                logger.debug("Result processor executor " + threadNum + " starting.");
                while (this.run)
                {
                    try
                    {
                        ResultMO result = this.consumer.pollResult();
                        if (result != null)
                        {
                            if (logger.isTraceEnabled())
                                logger.trace("Processing result\n" + result);
                            this.processExecuted(result);
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Error processing result", e);
                    }
                }
                logger.debug("Result processor executor " + threadNum + " stopped.");
            }, "Bergamot-Result-Processor-Executor-" + i);
        }
    }
    
    protected void publishAdhocResult(ResultMO result)
    {
        if (logger.isTraceEnabled()) logger.trace("Publishing adhoc result to " + result.getAdhocId() + " with message " + result);
    }

    protected void rescheduleCheck(ActiveCheck<?, ?> check, long interval)
    {
        if (logger.isTraceEnabled()) logger.trace("Rescheduling " + check.getType() + "::" + check.getId() + " [" + check.getName() + "]" + " with new interval " + interval + " due to state change");
        // Publish a message to the scheduler
        this.schedulerActions.publishSchedulerAction(new RescheduleCheck(check.getId(), interval));
    }

    protected void publishNotification(Check<?, ?> check, Notification notification)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending notification:\r\n" + notification);
        // Broadcast our notification first
        this.notificationBroker.publish(check.getSiteId(), notification);
        // Route to specific notification engines
        this.notificationProducer.sendNotification(notification);
    }
    
    protected void publishAlertUpdate(Alert alert, AlertUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending alert update:\r\n" + update);
        // Broadcast the update
        this.updateBroker.publish(alert.getSiteId(), update);
    }

    protected void publishCheckUpdate(Check<?, ?> check, CheckUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending check update:\r\n" + update);
        // Broadcast the update
        this.updateBroker.publish(check.getSiteId(), update);
    }
    
    protected void publishGroupUpdate(Group group, GroupUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending group update:\r\n" + update);
        // Broadcast the update
        this.updateBroker.publish(group.getSiteId(), update);
    }
    
    protected void publishLocationUpdate(Location location, LocationUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending location update:\r\n" + update);
        // Broadcast the update
        this.updateBroker.publish(location.getSiteId(), update);
    }
}

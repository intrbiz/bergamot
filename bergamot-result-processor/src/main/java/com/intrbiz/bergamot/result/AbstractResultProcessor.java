package com.intrbiz.bergamot.result;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.broker.SchedulingTopic;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.message.event.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.event.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.event.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.event.update.LocationUpdate;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck.Command;

public abstract class AbstractResultProcessor implements ResultProcessor
{
    private static final Logger logger = Logger.getLogger(AbstractResultProcessor.class);
    
    protected final SchedulingTopic schedulingTopic;
    
    protected final SiteNotificationTopic notificationTopic;
    
    protected final NotificationDispatcher notificationDispatcher;
    
    protected final SiteUpdateTopic updateTopic;

    public AbstractResultProcessor(SchedulingTopic schedulingTopic, NotificationDispatcher notificationDispatcher, SiteNotificationTopic notificationBroker, SiteUpdateTopic updateBroker)
    {
        super();
        this.schedulingTopic = schedulingTopic;
        this.notificationDispatcher = notificationDispatcher;
        this.notificationTopic = notificationBroker;
        this.updateTopic = updateBroker;
    }

    @Override
    public void start()
    {
    }
    
    @Override
    public void shutdown()
    {
    }
    
    protected void publishAdhocResult(ResultMessage result)
    {
        if (logger.isTraceEnabled()) logger.trace("Publishing adhoc result to " + result.getAdhocId() + " with message " + result);
        // TODO
    }

    protected void rescheduleCheck(ActiveCheck<?, ?> check, long interval)
    {
        if (logger.isTraceEnabled()) logger.trace("Rescheduling " + check.getType() + "::" + check.getId() + " [" + check.getName() + "]" + " with new interval " + interval + " due to state change");
        // Publish a message to the scheduler
        this.schedulingTopic.publish(new ScheduleCheck(check.getPool(), check.getId(), Command.RESCHEDULE, interval));
    }

    protected void publishNotification(Check<?, ?> check, Notification notification)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending notification:\r\n" + notification);
        if ("web".equalsIgnoreCase(notification.getEngine()))
        {
            // Broadcast web notification
            this.notificationTopic.publish(check.getSiteId(), notification);
        }
        else
        {
            // Route to specific notification engines
            this.notificationDispatcher.dispatchNotification(notification);
        }
    }
    
    protected void publishAlertUpdate(Alert alert, AlertUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending alert update:\r\n" + update);
        // Broadcast the update
        this.updateTopic.publish(alert.getSiteId(), update);
    }

    protected void publishCheckUpdate(Check<?, ?> check, CheckUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending check update:\r\n" + update);
        // Broadcast the update
        this.updateTopic.publish(check.getSiteId(), update);
    }
    
    protected void publishGroupUpdate(Group group, GroupUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending group update:\r\n" + update);
        // Broadcast the update
        this.updateTopic.publish(group.getSiteId(), update);
    }
    
    protected void publishLocationUpdate(Location location, LocationUpdate update)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending location update:\r\n" + update);
        // Broadcast the update
        this.updateTopic.publish(location.getSiteId(), update);
    }
}

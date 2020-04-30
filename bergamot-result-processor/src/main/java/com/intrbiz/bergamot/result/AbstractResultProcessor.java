package com.intrbiz.bergamot.result;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.broker.SchedulingTopic;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.NotificationType;
import com.intrbiz.bergamot.model.Notifications;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.event.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.event.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.event.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.event.update.LocationUpdate;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.SendAcknowledge;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.notification.SendUpdate;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck.Command;
import com.intrbiz.bergamot.model.state.CheckState;

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

    protected void publishNotification(UUID siteId, Notification notification)
    {
        if (logger.isTraceEnabled()) logger.trace("Sending notification:\r\n" + notification);
        if ("web".equalsIgnoreCase(notification.getEngine()))
        {
            // Broadcast web notification
            this.notificationTopic.publish(siteId, notification);
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
    
    protected Set<String> getNotificationEngines(UUID siteId)
    {
        Set<String> availableEngines = this.notificationDispatcher.getAvailableEngines(siteId);
        // add in the web notification engine which is internal
        availableEngines.add("web");
        return availableEngines;
    }
    
    protected <T extends CheckNotification> void publishNotification(Check<?, ?> check, CheckState state, Collection<Contact> contacts, Calendar now, NotificationType type, Supplier<T> ctor, Consumer<T> tweaker)
    {
        UUID siteId = check.getSiteId();
        Notifications checkNotifications = check.getNotifications();
        // compute the notifications to send
        for (String engine : this.getNotificationEngines(siteId))
        {
            // Should we send this notification?
            if (checkNotifications.isEnabledAt(type, state.getStatus(), now, engine))
            {
                // Filter the contacts
                List<ContactMO> filteredContacts = this.filterContactsToNotify(contacts, type, state.getStatus(), now, engine);
                if (! filteredContacts.isEmpty())
                {
                    // create the notifications
                    T notification = ctor.get();
                    notification.setEngine(engine);
                    notification.setSite(check.getSite().toMOUnsafe());
                    notification.setRaised(now.getTimeInMillis());
                    notification.setCheck(check.toMOUnsafe());
                    notification.setTo(filteredContacts);
                    tweaker.accept(notification);
                    // send it
                    try
                    {
                        this.publishNotification(siteId, notification);
                        // TODO: this.accounting.account(new SendNotificationAccountingEvent(check.getSiteId(), notification.getId(), check.getId(), AccountingNotificationType.ALERT, filteredContacts.size(), 0, null));
                    }
                    catch (Exception e)
                    {
                        logger.warn("Failed to send alert notification", e);
                    }
                }
            }
        }
    }
    
    protected List<ContactMO> filterContactsToNotify(Collection<Contact> contacts, NotificationType type, Status status, Calendar now, String engine)
    {
        return contacts.stream()
                .filter((contact) -> contact.getNotifications().isEnabledAt(type, status, now, engine))
                .map(Contact::toMOUnsafe)
                .collect(Collectors.toList());
    }

    public void publishAlertNotification(Check<?, ?> check, CheckState state, Alert alert, Collection<Contact> contacts, Calendar now)
    {
        this.publishNotification(check, state, contacts, now, NotificationType.ALERT, SendAlert::new, (n) -> { 
            n.setAlertId(alert.getId()); 
        });
    }

    public void publishEscalatedAlertNotification(Check<?, ?> check, CheckState state, Alert alert, Collection<Contact> contacts, Calendar now)
    {
        this.publishNotification(check, state, contacts, now, NotificationType.ALERT, SendAlert::new,  (n) -> { 
            n.setAlertId(alert.getId()); 
            n.setEscalation(true); 
        });
    }

    public void publishRecoveryNotification(Check<?, ?> check, CheckState state, Alert alert, Collection<Contact> contacts, Calendar now)
    {
        this.publishNotification(check, state, contacts, now, NotificationType.RECOVERY, SendRecovery::new, (n) -> { 
            n.setAlertId(alert.getId()); 
        });
    }

    public void publishAcknowledgeNotification(Check<?, ?> check, CheckState state, Alert alert, Collection<Contact> contacts, Calendar now, Contact acknowledgedBy, Comment acknowledgeComment)
    {
        this.publishNotification(check, state, contacts, now, NotificationType.ACKNOWLEDGE, SendAcknowledge::new, (n) -> {
            // additional detail for acknowledge
            n.setAcknowledgedBy(acknowledgedBy.toStubMOUnsafe());
            n.setAcknowledgeSummary(acknowledgeComment.getSummary());
            n.setAcknowledgeComment(acknowledgeComment.getComment());
        });
    }
    
    public void publishUpdateNotification(Check<?, ?> check, CheckState state, Collection<Contact> contacts, Calendar now)
    {
        this.publishNotification(check, state, contacts, now, NotificationType.UPDATE, SendUpdate::new, (n) -> {});
    }
}

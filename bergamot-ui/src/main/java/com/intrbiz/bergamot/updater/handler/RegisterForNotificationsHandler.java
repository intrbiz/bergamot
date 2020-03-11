package com.intrbiz.bergamot.updater.handler;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.notification.NotificationEvent;
import com.intrbiz.bergamot.model.message.api.notification.RegisterForNotifications;
import com.intrbiz.bergamot.model.message.api.notification.RegisteredForNotifications;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.updater.context.ClientContext;

public class RegisterForNotificationsHandler extends RequestHandler<RegisterForNotifications>
{
    private static final String VAR_NOTIFICATION_LISTENER_ID = "notification.listener.id";
    
    private Logger logger = Logger.getLogger(RegisterForNotificationsHandler.class);
    
    public RegisterForNotificationsHandler()
    {
        super(new Class<?>[] { RegisterForNotifications.class });
    }

    @Override
    public void onRequest(ClientContext context, RegisterForNotifications request)
    {
        // validate the site id
        if (! context.getSite().getId().equals(request.getSiteId()))
        {
            context.send(new APIError("Invalid site id given"));
        }
        else
        {
            // setup the listener if needed
            context.computeVarIfAbsent(VAR_NOTIFICATION_LISTENER_ID, (key) -> {
                // listen for notifications
                logger.debug("Registering for notifications, for site: " + context.getSite().getId());
                return context.app().getNotificationBroker().listen(context.getSite().getId(), (message) -> this.sendNotification(context, message));
            });
            // done
            context.send(new RegisteredForNotifications(request));
        }
    }
    
    private void sendNotification(ClientContext context, Notification notification)
    {
        if (notification instanceof CheckNotification)
        {
            if (context.getPrincipal().hasPermission("read", ((CheckNotification)notification).getCheck().getId()))
            {
                logger.debug("Sending notification to client: " + notification);
                context.send(new NotificationEvent((CheckNotification) notification));
            }
        }
    }
    
    public void onClose(ClientContext context)
    {
        String listenerId = context.removeVar(VAR_NOTIFICATION_LISTENER_ID);
        if (listenerId != null)
        {
            context.app().getNotificationBroker().unlisten(context.getSite().getId(), listenerId);
        }
    }
}

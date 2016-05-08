package com.intrbiz.bergamot.updater.handler;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.notification.NotificationEvent;
import com.intrbiz.bergamot.model.message.api.notification.RegisterForNotifications;
import com.intrbiz.bergamot.model.message.api.notification.RegisteredForNotifications;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.bergamot.updater.context.ClientContext;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueException;

public class RegisterForNotificationsHandler extends RequestHandler<RegisterForNotifications>
{
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
        else if (context.var("notificationConsumer") == null)
        {
            // listen for notifications
            logger.debug("Registering for notifications, for site: " + request.getSiteId());
            try
            {
                NotificationQueue notificationQueue = context.var("notificationQueue", NotificationQueue.open());
                context.var("notificationConsumer", notificationQueue.consumeNotifications((h, n) -> { this.sendNotification(context, n); }, request.getSiteId()));
                // on close
                context.onClose((ctx) -> {
                    Consumer<Notification, NotificationKey> c = ctx.var("notificationConsumer");
                    if (c != null) c.close();
                    NotificationQueue q = ctx.var("notificationQueue");
                    if (q != null) q.close();
                });
                // done
                context.send(new RegisteredForNotifications(request));
            }
            catch (QueueException e)
            {
                context.var("notificationQueue", null);
                context.var("notificationConsumer", null);
                logger.error("Failed to setup notification queue", e);
                context.send(new APIError("Failed to setup notification queue"));
            }
        }
        else
        {
            // done
            context.send(new RegisteredForNotifications(request));
        }
    }
    
    private void sendNotification(ClientContext context, Notification notification)
    {
        if (notification instanceof CheckNotification)
        {
            if (context.getPrincipal().hasPermission("", ((CheckNotification)notification).getCheck().getId()))
            {
                logger.debug("Sending notification to client: " + notification);
                context.send(new NotificationEvent((CheckNotification) notification));
            }
        }
    }
}

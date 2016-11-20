package com.intrbiz.bergamot.ui.action;

import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.notification.BackupCodeUsed;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.U2FADeviceRegistered;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.metadata.Action;
import com.intrbiz.queue.RoutedProducer;

public class U2FAActions
{
    private NotificationQueue queue;

    private RoutedProducer<Notification, NotificationKey> notificationProducer;

    public U2FAActions()
    {
        this.queue = NotificationQueue.open();
        this.notificationProducer = this.queue.publishNotifications();
    }

    @Action("u2fa-device-registered")
    public void u2faDeviceRegistered(Contact contact, String deviceName, String deviceType)
    {
        synchronized (this)
        {
            this.notificationProducer.publish(new NotificationKey(contact.getSiteId()), new U2FADeviceRegistered(contact.getSite().toMOUnsafe(), contact.toMOUnsafe().addEngine("email").addEngine("sms"), deviceName, deviceType));
        }
    }
    
    @Action("backup-code-used")
    public void backupCodeUsed(Contact contact, String code)
    {
        synchronized (this)
        {
            this.notificationProducer.publish(new NotificationKey(contact.getSiteId()), new BackupCodeUsed(contact.getSite().toMOUnsafe(), contact.toMOUnsafe().addEngine("email").addEngine("sms"), code));
        }
    }
}

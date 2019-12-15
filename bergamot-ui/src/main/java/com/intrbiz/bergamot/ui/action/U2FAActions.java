package com.intrbiz.bergamot.ui.action;

import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.metadata.Action;

public class U2FAActions
{
    public U2FAActions()
    {
    }

    @Action("u2fa-device-registered")
    public void u2faDeviceRegistered(Contact contact, String deviceName, String deviceType)
    {
        // TODO
        // this.notificationProducer.publish(new NotificationKey(contact.getSiteId()), new U2FADeviceRegistered(contact.getSite().toMOUnsafe(), contact.toMOUnsafe().addEngine("email").addEngine("sms"), deviceName, deviceType));
    }
    
    @Action("backup-code-used")
    public void backupCodeUsed(Contact contact, String code)
    {
        // TODO
        // this.notificationProducer.publish(new NotificationKey(contact.getSiteId()), new BackupCodeUsed(contact.getSite().toMOUnsafe(), contact.toMOUnsafe().addEngine("email").addEngine("sms"), code));
    }
}

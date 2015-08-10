package com.intrbiz.bergamot.ui.action;

import static com.intrbiz.balsa.BalsaContext.*;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Contact.LockOutReason;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.PasswordResetNotification;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.crypto.cookie.CookieBaker.Expires;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.metadata.Action;
import com.intrbiz.queue.RoutedProducer;

public class ContactActions
{
    private Logger logger = Logger.getLogger(ContactActions.class);
    
    private NotificationQueue notificationQueue;
    
    private RoutedProducer<Notification, NotificationKey> notificationsProducer;
    
    public ContactActions()
    {
        super();
        this.notificationQueue = NotificationQueue.open();
        this.notificationsProducer = this.notificationQueue.publishNotifications();
    }
    
    @Action("set-password")
    public boolean setPassword(Contact contact, String newPassword)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            // change it
            contact.hashPassword(newPassword);
            // store it
            logger.info("Setting password for contact " + contact.getSite().getName() + "::" + contact.getName() + " (" + contact.getId() + ")");
            db.setContact(contact);
            return true;
        }
    }
    
    @Action("reset-password")
    public boolean resetPassword(Contact contact)
    {
        if (contact != null)
        {
            // generate a token to authenticate the reset
            String token = Balsa().app().getSecurityEngine().generateAuthenticationTokenForPrincipal(contact, Expires.after(1, TimeUnit.DAYS), CryptoCookie.Flags.Reset);
            // construct the URL used for reset;
            String url = Balsa().url(Balsa().path("/reset")) + "?token=" + token; /* token is URL Safe */
            // force password change on the contact
            try (BergamotDB db = BergamotDB.connect())
            {
                db.setContact(contact.resetPassword());
            }
            // send a notification, only via email
            this.notificationsProducer.publish(
                    new NotificationKey(contact.getSite().getId()),
                    new PasswordResetNotification(contact.getSite().toMOUnsafe(), contact.toMOUnsafe().addEngine("email"), url)
            );
            logger.info("Sent password reset for contact " + contact.getSite().getName() + "::" + contact.getName() + " (" + contact.getId() + ")");
        }
        return true;
    }
    
    @Action("lock-contact")
    public boolean lock(Contact contact)
    {
        if (contact != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                db.setContact(contact.lock(LockOutReason.ADMINISTRATIVE));
            }
            logger.info("Locked contact " + contact.getSite().getName() + "::" + contact.getName() + " (" + contact.getId() + ")");
            // TODO: forcefully remove any sessions the contact has authenticated
        }
        return true;
    }
    
    @Action("unlock-contact")
    public boolean unlock(Contact contact)
    {
        if (contact != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                db.setContact(contact.unlock());
            }
            logger.info("Unlocked contact " + contact.getSite().getName() + "::" + contact.getName() + " (" + contact.getId() + ")");
        }
        return true;
    }
}

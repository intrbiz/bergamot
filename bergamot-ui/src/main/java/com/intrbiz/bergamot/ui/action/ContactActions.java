package com.intrbiz.bergamot.ui.action;

import static com.intrbiz.balsa.BalsaContext.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.NotificationEngineCfg;
import com.intrbiz.bergamot.config.model.NotificationsCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Contact.LockOutReason;
import com.intrbiz.bergamot.model.NotificationEngine;
import com.intrbiz.bergamot.model.Notifications;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.TimePeriod;
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
    
    @Action("create-contact-template")
    public void createContactTemplate(ContactCfg config)
    {
        if (config.getId() == null) throw new IllegalArgumentException("Config must have a valid Id");
        try (BergamotDB db = BergamotDB.connect())
        {
            // resolve the config
            db.getConfigResolver(Site.getSiteId(config.getId())).resolveInherit(config);
            logger.info("Storing contact template:\n" + config.toString());
            // store the config
            db.setConfig(new Config(config.getId(), Site.getSiteId(config.getId()), config));
            // update any entities that this template alters
        }
    }
    
    @Action("create-contact")
    public Contact createContact(ContactCfg config)
    {
        if (config.getId() == null) throw new IllegalArgumentException("Config must have a valid Id");
        if (config.getTemplateBooleanValue()) throw new IllegalArgumentException("Cannot create a contact from a template");
        try (BergamotDB db = BergamotDB.connect())
        {
            // resolve the config
            db.getConfigResolver(Site.getSiteId(config.getId())).resolveInherit(config);
            logger.info("Creating contact from config:\n" + config.resolve().toString());
            // store the config
            db.setConfig(new Config(config.getId(), Site.getSiteId(config.getId()), config));
            // create the contact
            ContactCfg rcfg = config.resolve();
            Contact contact = new Contact();
            contact.configure(rcfg);
            // store it
            logger.info("Storing contact: " + contact.toJSON());
            db.setContact(contact);
            // notifications
            this.loadNotifications(Site.getSiteId(contact.getId()), contact.getId(), rcfg.getNotifications(), db);
            // teams
            for (String teamName : rcfg.getTeams())
            {
                Team team = db.getTeamByName(Site.getSiteId(contact.getId()), teamName);
                if (team != null)
                {
                    logger.info("Adding contact " + contact.getName() + " to team " + team.getName());
                    team.addContact(contact);
                }
            }
            return contact;
        }
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
                    new PasswordResetNotification(contact.getSite().toMO(), contact.toMO().addEngine("email"), url)
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
    
    private void loadNotifications(UUID siteId, UUID owner, NotificationsCfg cfg, BergamotDB db)
    {
        Notifications notifications = new Notifications();
        notifications.setId(owner);
        notifications.setEnabled(cfg.getEnabledBooleanValue());
        notifications.setAlertsEnabled(cfg.getAlertsBooleanValue());
        notifications.setRecoveryEnabled(cfg.getRecoveryBooleanValue());
        notifications.setIgnore(cfg.getIgnore().stream().map((e) -> {return Status.valueOf(e.toUpperCase());}).collect(Collectors.toList()));
        notifications.setAllEnginesEnabled(cfg.getAllEnginesEnabledBooleanValue());
        // load the time period
        if (! Util.isEmpty(cfg.getNotificationPeriod()))
        {
            TimePeriod timePeriod = db.getTimePeriodByName(siteId, cfg.getNotificationPeriod());
            if (timePeriod != null)
            {
                notifications.setTimePeriodId(timePeriod.getId());
            }
        }
        db.setNotifications(notifications);
        // engines
        for (NotificationEngineCfg ecfg : cfg.getNotificationEngines())
        {
            NotificationEngine notificationEngine = new NotificationEngine();
            notificationEngine.setNotificationsId(notifications.getId());
            notificationEngine.setEngine(ecfg.getEngine());
            notificationEngine.setEnabled(ecfg.getEnabledBooleanValue());
            notificationEngine.setAlertsEnabled(ecfg.getAlertsBooleanValue());
            notificationEngine.setRecoveryEnabled(ecfg.getRecoveryBooleanValue());
            notificationEngine.setIgnore(ecfg.getIgnore().stream().map((e) -> {return Status.valueOf(e.toUpperCase()); }).collect(Collectors.toList()));
            if (! Util.isEmpty(ecfg.getNotificationPeriod()))
            {
                TimePeriod timePeriod = db.getTimePeriodByName(siteId, ecfg.getNotificationPeriod());
                if (timePeriod != null)
                {
                    notificationEngine.setTimePeriodId(timePeriod.getId());
                }
            }
            db.setNotificationEngine(notificationEngine);
        }
    }
}

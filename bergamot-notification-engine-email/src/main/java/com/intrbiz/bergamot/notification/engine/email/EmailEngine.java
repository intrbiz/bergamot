package com.intrbiz.bergamot.notification.engine.email;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.SendNotificationToContactAccountingEvent;
import com.intrbiz.bergamot.health.HealthTracker;
import com.intrbiz.bergamot.health.model.KnownDaemon;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.GenericNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.notification.AbstractNotificationEngine;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.QueueException;

public class EmailEngine extends AbstractNotificationEngine
{
    public static final String NAME = "email";

    private Logger logger = Logger.getLogger(EmailEngine.class);

    private Properties properties = new Properties();

    private Session session;

    private String user;

    private String password;

    private String fromAddress;
    
    private final Timer emailSendTimer;
    
    private final Counter emailSendErrors;
    
    private Accounting accounting = Accounting.create(AbstractNotificationEngine.class);
    
    private List<String> healthcheckAdmins = new LinkedList<String>();

    public EmailEngine()
    {
        super(NAME);
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.email");
        this.emailSendTimer = source.getRegistry().timer("email-sent");
        this.emailSendErrors = source.getRegistry().counter("email-errors");
    }

    public List<String> getHealthcheckAdmins()
    {
        return healthcheckAdmins;
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        // setup the JavaMail properties
        this.properties = new Properties();
        this.properties.put("mail.smtp.host", this.config.getStringParameterValue("mail.host", "127.0.0.1"));
        this.properties.put("mail.smtp.port", this.config.getStringParameterValue("mail.port", "25"));
        if (this.config.getBooleanParameterValue("mail.tls", false))
        {
            this.properties.put("mail.smtp.port", this.config.getStringParameterValue("mail.port", "587"));
            this.properties.put("mail.smtp.starttls.enable", true);
            this.properties.put("mail.smtp.auth", true);
        }
        this.properties.put("mail.smtp.from", this.config.getStringParameterValue("from", "bergamot@localhost"));
        // auth details
        this.user = this.config.getStringParameterValue("mail.user", "");
        this.password = this.config.getStringParameterValue("mail.password", "");
        // from address
        this.fromAddress = this.config.getStringParameterValue("from", "bergamot@localhost");
        // setup the JavaMail session
        this.session = Session.getDefaultInstance(properties);
        this.session.setDebug(true);
        // who to contact in the event we get a warning from the healthcheck subsystem
        for (CfgParameter param : this.config.getParameters())
        {
            if ("healthcheck.admin".equals(param.getName()) && (! Util.isEmpty(param.getValueOrText())))
                this.healthcheckAdmins.add(param.getValueOrText());
        }
        logger.info("Healthcheck alerts will be sent to " + this.healthcheckAdmins);
        // setup healthchecking
        HealthTracker.getInstance().addAlertHandler(this::raiseHealthcheckAlert);
    }
    
    public void raiseHealthcheckAlert(KnownDaemon failed)
    {
        logger.error("Got healthcheck alert for " + failed.getDaemonName() + " [" + failed.getInstanceId() + "] on host " + failed.getHostName() + " [" + failed.getHostId() + "]");
        if (! this.healthcheckAdmins.isEmpty())
        {
            // really try to send
            for (int i = 0; i < 10; i++)
            {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                try
                {
                    // build the message
                    Message message = this.buildHealhCheckMessage(this.session, failed);
                    // send the message
                    Transport transport = null;
                    try
                    {
                        transport = session.getTransport("smtp");
                        // connect
                        if (Util.isEmpty(this.user))
                        {
                            logger.debug("Transport connecting to: " + this.properties.getProperty("mail.smtp.host"));
                            transport.connect();
                            logger.debug("Transport connected");
                        }
                        else
                        {
                            logger.debug("Transport connecting to: " + this.properties.getProperty("mail.smtp.host") + " with username: " + this.user);
                            transport.connect(this.user, this.password);
                            logger.debug("Transport connected");
                        }
                        // send the message
                        logger.info("Sending healthcheck message...");
                        transport.sendMessage(message, message.getAllRecipients());
                        logger.info("Message healthcheck sent");
                    }
                    finally
                    {
                        if (transport != null) transport.close();
                    }
                    // successfully sent
                    break;
                }
                catch (Throwable e)
                {
                    logger.error("Failed to send healthcheck email notification", e);
                }
                // pause after each attempt
                if (i > 0)
                {
                    try
                    {
                        Thread.sleep(i * 1000);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        }
    }
    
    protected Message buildHealhCheckMessage(Session session, KnownDaemon daemon) throws Exception
    {
        Message message = new MimeMessage(session);
        // from
        message.setFrom(new InternetAddress(this.fromAddress));
        // to address
        for (String admin : this.healthcheckAdmins)
        {
            message.addRecipient(RecipientType.TO, new InternetAddress(admin));
        }
        // sent date
        message.setSentDate(new Date());
        // content
        message.setSubject(this.applyTemplate("healthcheck.alert.subject", daemon));
        message.setContent(this.applyTemplate("healthcheck.alert.content", daemon), "text/plain");
        return message;
    }

    @Override
    public void sendNotification(Notification notification)
    {
        if (logger.isTraceEnabled()) logger.trace(notification.toString());
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        logger.info("Sending email notification for " + notification.getNotificationType() + " to " + notification.getTo().stream().map(ContactMO::getEmail).filter((e) -> { return e != null; }).collect(Collectors.toList()));
        Timer.Context tctx = this.emailSendTimer.time();
        try
        {
            if (! this.checkAtLeastOneRecipient(notification))
            {
                logger.info("Not sending email, no recipients");
                return;
            }
            // build the message
            Message message = this.buildMessage(notification);
            logger.debug("Built message");
            // send the message
            Transport transport = null;
            try
            {
                transport = session.getTransport("smtp");
                // connect
                if (Util.isEmpty(this.user))
                {
                    logger.debug("Transport connecting to: " + this.properties.getProperty("mail.smtp.host"));
                    transport.connect();
                    logger.debug("Transport connected");
                }
                else
                {
                    logger.debug("Transport connecting to: " + this.properties.getProperty("mail.smtp.host") + " with username: " + this.user);
                    transport.connect(this.user, this.password);
                    logger.debug("Transport connected");
                }
                // send the message
                logger.info("Sending message...");
                transport.sendMessage(message, message.getAllRecipients());
                logger.info("Message sent");
                // accounting
                for (ContactMO contact : notification.getTo())
                {
                    if ((!Util.isEmpty(contact.getEmail())) && contact.hasEngine(this.getName()))
                    {
                        this.accounting.account(new SendNotificationToContactAccountingEvent(
                            notification.getSite().getId(),
                            notification.getId(),
                            getObjectId(notification),
                            getNotificationType(notification),
                            contact.getId(),
                            this.getName(),
                            "email",
                            contact.getEmail(),
                            Util.nullable(message.getHeader("Message-ID"), (s) -> s.length  == 0 ? null : s[0])
                        ));
                    }
                }
            }
            finally
            {
                if (transport != null) transport.close();
            }
        }
        catch (Throwable e)
        {
            this.emailSendErrors.inc();
            logger.error("Failed to send email notification", e);
            throw new QueueException("Failed to send email notification", e);
        }
        finally
        {
            tctx.stop();
        }
    }
    
    protected boolean checkAtLeastOneRecipient(Notification notification)
    {
        for (ContactMO contact : notification.getTo())
        {
            if ((!Util.isEmpty(contact.getEmail())) && contact.hasEngine(this.getName()))
            {
                return true;
            }
        }
        return false;
    }
    
    protected Message buildMessage(Notification notification) throws Exception
    {
        if (notification instanceof CheckNotification)
        {
            return this.buildCheckMessage((CheckNotification) notification);
        }
        else if (notification instanceof GenericNotification)
        {
            return this.buildGenericMessage((GenericNotification) notification);
        }
        return null;
    }
    
    protected Message buildGenericMessage(GenericNotification notification) throws Exception
    {
        Message message = new MimeMessage(this.session);
        // from
        message.setFrom(new InternetAddress(this.fromAddress));
        // to address
        for (ContactMO contact : notification.getTo())
        {
            if ((!Util.isEmpty(contact.getEmail())) && contact.hasEngine(this.getName()))
            {
                message.addRecipient(RecipientType.TO, new InternetAddress(contact.getEmail()));
            }
        }
        // headers
        message.setHeader("X-Bergamot-Notification-Id", notification.getId().toString());
        // sent date
        message.setSentDate(new Date(notification.getRaised()));
        // content
        this.buildGenericMessageContent(message, notification);
        return message;
    }

    protected Message buildCheckMessage(CheckNotification notification) throws Exception
    {
        Message message = new MimeMessage(this.session) {
            @Override
            protected void updateMessageID() throws MessagingException
            {
                // do not alter the message id
            }
        };
        // from
        message.setFrom(new InternetAddress(this.fromAddress));
        // to address
        for (ContactMO contact : notification.getTo())
        {
            if ((!Util.isEmpty(contact.getEmail())) && contact.hasEngine(this.getName()))
            {
                message.addRecipient(RecipientType.TO, new InternetAddress(contact.getEmail()));
            }
        }
        // headers
        message.setHeader("X-Bergamot-Alert-Id", notification.getAlertId().toString());
        message.setHeader("Message-ID", this.checkMessageId(notification));
        message.setHeader("X-Bergamot-Notification-Id", notification.getId().toString());
        message.setHeader("X-Bergamot-Check-Type", notification.getCheck().getType());
        message.setHeader("X-Bergamot-Check-Id", notification.getCheck().getId().toString());
        message.setHeader("X-Bergamot-Check-Status", notification.getCheck().getState().getStatus().toString());
        // in reply to?
        if (notification instanceof SendRecovery)
        {
            message.setHeader("References", this.checkMessageId(notification));
            message.setHeader("In-Reply-To", this.checkMessageId(notification));
        }
        // sent date
        message.setSentDate(new Date(notification.getRaised()));
        // content
        this.buildCheckMessageContent(message, notification);
        return message;
    }
    
    protected String checkMessageId(CheckNotification notification)
    {
        return "<" + notification.getAlertId().toString() + ".bergamot>";
    }

    protected void buildCheckMessageContent(Message message, CheckNotification notification) throws Exception
    {
        String templatePrefix = notification.getCheck().getCheckType() + "." + notification.getNotificationType() + ".";
        message.setSubject(this.applyTemplate(templatePrefix + "subject", notification));
        message.setContent(this.applyTemplate(templatePrefix + "content", notification), "text/plain");
    }
    
    protected void buildGenericMessageContent(Message message, GenericNotification notification) throws Exception
    {
        String templatePrefix = notification.getNotificationType() + ".";
        message.setSubject(this.applyTemplate(templatePrefix + "subject", notification));
        message.setContent(this.applyTemplate(templatePrefix + "content", notification), "text/plain");
    }
}

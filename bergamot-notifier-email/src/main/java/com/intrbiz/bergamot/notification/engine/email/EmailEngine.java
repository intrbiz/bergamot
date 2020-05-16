package com.intrbiz.bergamot.notification.engine.email;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;
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
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.accounting.model.SendNotificationToContactAccountingEvent;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.GenericNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.notification.AbstractNotificationEngine;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.template.TemplatedNotificationEngine;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

public class EmailEngine extends TemplatedNotificationEngine
{
    public static final String NAME = "email";

    private static final Logger logger = Logger.getLogger(EmailEngine.class);

    private Properties properties = new Properties();

    private Session session;

    private String user;

    private String password;

    private String fromAddress;
    
    private final Timer emailSendTimer;
    
    private final Counter emailSendErrors;
    
    private Accounting accounting = Accounting.create(AbstractNotificationEngine.class);

    public EmailEngine()
    {
        super(BergamotVersion.NAME, NAME, true);
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.email");
        this.emailSendTimer = source.getRegistry().timer("email-sent");
        this.emailSendErrors = source.getRegistry().counter("email-errors");
    }

    @Override
    protected void doPrepare(NotificationEngineContext engineContext) throws Exception
    {
        super.doPrepare(engineContext);
        // setup the JavaMail properties
        this.properties = new Properties();
        this.properties.put("mail.smtp.host", engineContext.getParameter("mail.host", "127.0.0.1"));
        this.properties.put("mail.smtp.port", engineContext.getParameter("mail.port", "25"));
        if (engineContext.getBooleanParameter("mail.tls", false))
        {
            this.properties.put("mail.smtp.port", engineContext.getParameter("mail.port", "587"));
            this.properties.put("mail.smtp.starttls.enable", true);
            this.properties.put("mail.smtp.auth", true);
        }
        this.properties.put("mail.smtp.from", engineContext.getParameter("mail.from", "bergamot@localhost"));
        // auth details
        this.user = engineContext.getParameter("mail.user", "");
        this.password = engineContext.getParameter("mail.password", "");
        // from address
        this.fromAddress = engineContext.getParameter("mail.from", "bergamot@localhost");
        // setup the JavaMail session
        this.session = Session.getDefaultInstance(properties);
        this.session.setDebug(true);
    }

    @Override
    public boolean accept(Notification notification)
    {
        return this.checkAtLeastOneRecipient(notification);
    }

    @Override
    public void sendNotification(Notification notification)
    {
        if (logger.isTraceEnabled()) logger.trace(notification.toString());
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        logger.info("Sending email notification for " + notification.getNotificationType() + " to " + notification.getTo().stream().map(ContactMO::getEmail).filter((e) -> { return e != null; }).collect(Collectors.toList()));
        try (Timer.Context tctx = this.emailSendTimer.time())
        {
            // build the message
            Message message = this.buildMessage(notification);
            if (logger.isDebugEnabled())
                logger.debug("Built message: " + message);
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
                    if ((!Util.isEmpty(contact.getEmail())))
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
        }
    }
    
    protected boolean checkAtLeastOneRecipient(Notification notification)
    {
        for (ContactMO contact : notification.getTo())
        {
            if ((!Util.isEmpty(contact.getEmail())))
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
            if ((!Util.isEmpty(contact.getEmail())))
            {
                message.addRecipient(RecipientType.TO, new InternetAddress(contact.getEmail()));
            }
        }
        // headers
        message.setHeader("Message-ID", this.messageId(notification.getId(), notification.getNotificationType()));
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
            if ((!Util.isEmpty(contact.getEmail())))
            {
                message.addRecipient(RecipientType.TO, new InternetAddress(contact.getEmail()));
            }
        }
        // headers
        // message ids
        if (notification instanceof SendAlert)
        {
            UUID alertId = ((SendAlert) notification).getAlertId();
            if (alertId != null) message.setHeader("X-Bergamot-Alert-Id", alertId.toString());
            message.setHeader("Message-ID", this.messageId(alertId, "alert"));
        }
        else if (notification instanceof SendRecovery)
        {
            UUID alertId = ((SendRecovery) notification).getAlertId();
            if (alertId != null) message.setHeader("X-Bergamot-Alert-Id", alertId.toString());
            message.setHeader("Message-ID", this.messageId(alertId, "recovery"));
            // reply to
            String alertMessageId = this.messageId(alertId, "alert");
            message.setHeader("References", alertMessageId);
            message.setHeader("In-Reply-To", alertMessageId);
        }
        else
        {
            message.setHeader("Message-ID", this.messageId(notification.getId(), notification.getNotificationType()));
        }
        // helper headers
        message.setHeader("X-Bergamot-Notification-Id", notification.getId().toString());
        message.setHeader("X-Bergamot-Check-Type", notification.getCheck().getType());
        message.setHeader("X-Bergamot-Check-Id", notification.getCheck().getId().toString());
        message.setHeader("X-Bergamot-Check-Status", notification.getCheck().getState().getStatus().toString());
        // sent date
        message.setSentDate(new Date(notification.getRaised()));
        // content
        this.buildCheckMessageContent(message, notification);
        return message;
    }
    
    protected String messageId(UUID id, String type)
    {
        if (id == null) id = UUID.randomUUID();
        return "<" + id + "." + type + ".bergamot>";
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

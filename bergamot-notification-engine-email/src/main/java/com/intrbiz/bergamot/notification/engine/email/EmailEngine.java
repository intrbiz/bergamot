package com.intrbiz.bergamot.notification.engine.email;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
            if (! this.checkAtLeastOneRecipient(notification)) return;
            // build the message
            Message message = this.buildMessage(this.session, notification);
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
    
    protected Message buildMessage(Session session, Notification notification) throws Exception
    {
        if (notification instanceof CheckNotification)
        {
            return this.buildCheckMessage(session, (CheckNotification) notification);
        }
        else if (notification instanceof GenericNotification)
        {
            return this.buildGenericMessage(session, (GenericNotification) notification);
        }
        return null;
    }
    
    protected Message buildGenericMessage(Session session, GenericNotification notification) throws Exception
    {
        Message message = new MimeMessage(session);
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

    protected Message buildCheckMessage(Session session, CheckNotification notification) throws Exception
    {
        Message message = new MimeMessage(session) {
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
    
    @Override
    protected Map<String, String> getDefaultTemplates()
    {        
        Map<String, String> templates = new HashMap<String, String>();
        // host
        templates.put("host.acknowledge.subject", "#{notification.acknowledgedBy.summary} has acknowledged an alert for host #{host.summary}");
        templates.put("host.acknowledge.content", "The alert for host #{host.summary} has been acknowledged by "
                + "#{notification.acknowledgedBy.summary} #{dateformat('HH:mm:ss', notification.raised)} "
                + "on #{dateformat('EEEE dd/MM/yyyy', notification.raised)} with the following comment:\r\n"
                + "\r\n"
                + "#{notification.acknowledgeSummary}\r\n"
                + "\r\n"
                + "#{notification.acknowledgeComment}\r\n"
                + "Thank you, Bergamot");
        templates.put("host.alert.subject", "Alert for host #{host.summary} is #{if(host.state.ok, 'UP', 'DOWN')}");
        templates.put("host.alert.content", "Alert for host #{host.summary} is #{if(host.state.ok, 'UP', 'DOWN')}\r\n"
                + "\r\n"
                + "Check output: #{host.state.output}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', host.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', host.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/host/id/#{host.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        templates.put("host.recovery.subject", "Recovery for host #{host.summary} is #{if(host.state.ok, 'UP', 'DOWN')}");
        templates.put("host.recovery.content", "Recovery for host #{host.summary} is #{if(host.state.ok, 'UP', 'DOWN')}\r\n"
                + "\r\n"
                + "Check output: #{host.state.output}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', host.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', host.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/host/id/#{host.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        // cluster
        templates.put("cluster.acknowledge.subject", "#{notification.acknowledgedBy.summary} has acknowledged an alert for cluster #{cluster.summary}");
        templates.put("cluster.acknowledge.content", "The alert for cluster #{cluster.summary} has been acknowledged by "
                + "#{notification.acknowledgedBy.summary} #{dateformat('HH:mm:ss', notification.raised)} "
                + "on #{dateformat('EEEE dd/MM/yyyy', notification.raised)} with the following comment:\r\n"
                + "\r\n"
                + "#{notification.acknowledgeSummary}\r\n"
                + "\r\n"
                + "#{notification.acknowledgeComment}\r\n"
                + "Thank you, Bergamot");
        templates.put("cluster.alert.subject", "Alert for cluster #{cluster.summary} is #{if(cluster.state.ok, 'UP', 'DOWN')}");
        templates.put("cluster.alert.content", "Alert for cluster #{cluster.summary} is #{if(cluster.state.ok, 'UP', 'DOWN')}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', cluster.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', cluster.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/cluster/id/#{cluster.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        templates.put("cluster.recovery.subject", "Recovery for cluster #{cluster.summary} is #{if(cluster.state.ok, 'UP', 'DOWN')}");
        templates.put("cluster.recovery.content", "Recovery for cluster #{cluster.summary} is #{if(cluster.state.ok, 'UP', 'DOWN')}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', cluster.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', cluster.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/cluster/id/#{cluster.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        // service
        templates.put("service.acknowledge.subject", "#{notification.acknowledgedBy.summary} has acknowledged an alert for service #{service.summary} on the host #{host.summary}");
        templates.put("service.acknowledge.content", "The alert for service #{service.summary} on the host #{host.summary} has been acknowledged by "
                + "#{notification.acknowledgedBy.summary} #{dateformat('HH:mm:ss', notification.raised)} "
                + "on #{dateformat('EEEE dd/MM/yyyy', notification.raised)} with the following comment:\r\n"
                + "\r\n"
                + "#{notification.acknowledgeSummary}\r\n"
                + "\r\n"
                + "#{notification.acknowledgeComment}\r\n"
                + "Thank you, Bergamot");
        templates.put("service.alert.subject", "Alert for service #{service.summary} on the host #{host.summary} is #{service.state.status}");
        templates.put("service.alert.content", "Alert for service #{service.summary} on the host #{host.summary} is #{service.state.status}\r\n"
                + "\r\n"
                + "Check output: #{service.state.output}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', service.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', service.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/service/id/#{service.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        templates.put("service.recovery.subject", "Recovery for service #{service.summary} on the host #{host.summary} is #{service.state.status}");
        templates.put("service.recovery.content", "Recovery for service #{service.summary} on the host #{host.summary} is #{service.state.status}\r\n"
                + "\r\n"
                + "Check output: #{service.state.output}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', service.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', service.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/service/id/#{service.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        // trap
        templates.put("trap.acknowledge.subject", "#{notification.acknowledgedBy.summary} has acknowledged an alert for trap #{trap.summary} on the host #{host.summary}");
        templates.put("trap.acknowledge.content", "The alert for trap #{trap.summary} on the host #{host.summary} has been acknowledged by "
                + "#{notification.acknowledgedBy.summary} #{dateformat('HH:mm:ss', notification.raised)} "
                + "on #{dateformat('EEEE dd/MM/yyyy', notification.raised)} with the following comment:\r\n"
                + "\r\n"
                + "#{notification.acknowledgeSummary}\r\n"
                + "\r\n"
                + "#{notification.acknowledgeComment}\r\n"
                + "Thank you, Bergamot");
        templates.put("trap.alert.subject", "Alert for trap #{trap.summary} on the host #{host.summary} is #{trap.state.status}");
        templates.put("trap.alert.content", "Alert for trap #{trap.summary} on the host #{host.summary} is #{trap.state.status}\r\n"
                + "\r\n"
                + "Check output: #{trap.state.output}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', trap.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', trap.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/trap/id/#{trap.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        templates.put("trap.recovery.subject", "Recovery for trap #{trap.summary} on the host #{host.summary} is #{trap.state.status}");
        templates.put("trap.recovery.content", "Recovery for trap #{trap.summary} on the host #{host.summary} is #{trap.state.status}\r\n"
                + "\r\n"
                + "Check output: #{trap.state.output}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', trap.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', trap.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/trap/id/#{trap.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        // resource
        templates.put("resource.acknowledge.subject", "#{notification.acknowledgedBy.summary} has acknowledged an alert for resource #{resource.summary} on the cluster #{cluster.summary}");
        templates.put("resource.acknowledge.content", "The alert for resource #{resource.summary} on the cluster #{cluster.summary} has been acknowledged by "
                + "#{notification.acknowledgedBy.summary} #{dateformat('HH:mm:ss', notification.raised)} "
                + "on #{dateformat('EEEE dd/MM/yyyy', notification.raised)} with the following comment:\r\n"
                + "\r\n"
                + "#{notification.acknowledgeSummary}\r\n"
                + "\r\n"
                + "#{notification.acknowledgeComment}\r\n"
                + "Thank you, Bergamot");
        templates.put("resource.alert.subject", "Alert for resource #{resource.summary} on the cluster #{cluster.summary} is #{resource.state.status}");
        templates.put("resource.alert.content", "Alert for resource #{resource.summary} on the cluster #{cluster.summary} is #{resource.state.status}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', resource.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', resource.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/resource/id/#{resource.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        templates.put("resource.recovery.subject", "Recovery for resource #{resource.summary} on the cluster #{cluster.summary} is #{resource.state.status}");
        templates.put("resource.recovery.content", "Recovery for resource #{resource.summary} on the cluster #{cluster.summary} is #{resource.state.status}\r\n"
                + "\r\n"
                + "Last checked at #{dateformat('HH:mm:ss', resource.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', resource.state.lastCheckTime)}\r\n"
                + "\r\n"
                + "For more information: https://#{site.name}/resource/id/#{resource.id}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        // generic
        // password reset
        templates.put("password_reset.subject", "Password reset for #{contact.name} at #{coalesce(site.summary, site.name)}");
        templates.put("password_reset.content", "Hello #{coalesce(contact.summary, contact.name)}\r\n"
                + "\r\n"
                + "To reset your password on #{coalesce(site.summary, site.name)}, please follow the link below:\r\n"
                + "\r\n"
                + "#{notification.url}\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        // register_contact
        templates.put("register_contact.subject", "Welcome #{contact.name} to Bergamot Monitoring");
        templates.put("register_contact.content", "Hello #{coalesce(contact.summary, contact.name)}\r\n"
                + "\r\n"
                + "Welcome to Bergamot Monitoring.  To activate your account on #{coalesce(site.summary, site.name)}, please follow the link below:\r\n"
                + "\r\n"
                + "#{notification.url}\r\n"
                + "\r\n"
                + "You can login at: https://#{site.name}/\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        // healthcheck
        templates.put("healthcheck.alert.subject", "[Critical] Bergamot Monitoring is not healthy");
        templates.put("healthcheck.alert.content", "Hello\r\n"
                + "\r\n"
                + "Bergamot Monitoring has detected that an internal component has failed.\r\n"
                + "\r\n"
                + "The daemon #{daemon.daemonName} (instance #{daemon.instanceId}) on the host #{daemon.hostName} (#{daemon.hostId}) has failed.\r\n"
                + "\r\n"
                + "Thank you, Bergamot");
        return templates;
    }
}

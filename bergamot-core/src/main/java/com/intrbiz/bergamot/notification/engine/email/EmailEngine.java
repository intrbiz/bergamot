package com.intrbiz.bergamot.notification.engine.email;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.notification.AbstractNotificationEngine;
import com.intrbiz.express.DefaultContext;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.ExpressExtensionRegistry;
import com.intrbiz.express.action.ActionHandler;
import com.intrbiz.express.value.ValueExpression;

public class EmailEngine extends AbstractNotificationEngine
{
    public static final String NAME = "email";

    private Logger logger = Logger.getLogger(EmailEngine.class);

    private Properties properties = new Properties();

    private Session session;

    private String user;

    private String password;

    private String fromAddress;

    //

    private final ExpressExtensionRegistry expressExtensions = new ExpressExtensionRegistry("bergamot").addSubRegistry(ExpressExtensionRegistry.getDefaultRegistry());

    //

    private ValueExpression serviceAlertSubject;

    private ValueExpression serviceAlertContent;

    //
    
    private ValueExpression serviceRecoverySubject;

    private ValueExpression serviceRecoveryContent;
    
    //

    private ValueExpression hostAlertSubject;

    private ValueExpression hostAlertContent;

    //
    
    private ValueExpression hostRecoverySubject;

    private ValueExpression hostRecoveryContent;

    public EmailEngine()
    {
        super(NAME);
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
        // load the templates
        ExpressContext context = this.createContext(null);
        // service
        this.serviceAlertSubject = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("service.alert.subject", Templates.Service.Alert.SUBJECT)));
        this.serviceAlertContent = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("service.alert.content", Templates.Service.Alert.CONTENT)));
        this.serviceRecoverySubject = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("service.recovery.subject", Templates.Service.Recovery.SUBJECT)));
        this.serviceRecoveryContent = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("service.recovery.content", Templates.Service.Recovery.CONTENT)));
        // host
        this.hostAlertSubject = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("host.alert.subject", Templates.Host.Alert.SUBJECT)));
        this.hostAlertContent = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("host.alert.content", Templates.Host.Alert.CONTENT)));
        this.hostRecoverySubject = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("host.recovery.subject", Templates.Host.Recovery.SUBJECT)));
        this.hostRecoveryContent = new ValueExpression(context, Util.coalesce(this.config.getStringParameterValue("host.recovery.content", Templates.Host.Recovery.CONTENT)));
        //
        logger.info("Using service alert template: \r\nSubject: " + this.serviceAlertSubject + "\r\n\r\n" + this.serviceAlertContent);
        logger.info("Using service recovery template: \r\nSubject: " + this.serviceRecoverySubject + "\r\n\r\n" + this.serviceRecoveryContent);
        logger.info("Using host alert template: \r\nSubject: " + this.hostAlertSubject + "\r\n\r\n" + this.hostAlertContent);
        logger.info("Using host recovery template: \r\nSubject: " + this.hostRecoverySubject + "\r\n\r\n" + this.hostRecoveryContent);
    }

    @Override
    public void sendNotification(Notification notification)
    {
        logger.info("Sending notification for " + notification.getCheck() + " to " + notification.getTo());
        try
        {
            if (notification.getTo().isEmpty()) throw new RuntimeException("Cannot send notifications, no To addresses given.");
            Transport transport = null;
            try
            {
                transport = session.getTransport("smtp");
                // build the message
                Message message = this.buildMessage(this.session, notification);
                // connect
                if (Util.isEmpty(this.user))
                    transport.connect();
                else
                    transport.connect(this.user, this.password);
                // send the message
                transport.sendMessage(message, message.getAllRecipients());
            }
            finally
            {
                if (transport != null) transport.close();
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to send notification", e);
        }

    }

    protected Message buildMessage(Session session, Notification notification) throws Exception
    {
        Message message = new MimeMessage(session);
        // from
        message.setFrom(new InternetAddress(this.fromAddress));
        // to address
        for (ContactMO contact : notification.getTo())
        {
            message.addRecipient(RecipientType.TO, new InternetAddress(contact.getEmail()));
        }
        // headers
        message.setHeader("X-Bergamot-Notification-Id", notification.getId().toString());
        message.setHeader("X-Bergamot-Check-Type", notification.getCheck().getType());
        message.setHeader("X-Bergamot-Check-Id", notification.getCheck().getId().toString());
        message.setHeader("X-Bergamot-Check-Status", notification.getCheck().getState().getStatus().toString());
        // sent date
        message.setSentDate(new Date(notification.getRaised()));
        // content
        this.buildMessageContent(message, notification);
        return message;
    }

    protected void buildMessageContent(Message message, Notification notification) throws Exception
    {
        ExpressContext context = this.createContext(notification);
        if (notification instanceof SendAlert)
        {
            logger.debug("Building alert message content");
            if (notification.getCheck() instanceof ServiceMO)
            {
                message.setSubject(String.valueOf(this.serviceAlertSubject.get(context, notification)));
                message.setContent(String.valueOf(this.serviceAlertContent.get(context, notification)), "text/plain");
            }
            else if (notification.getCheck() instanceof HostMO)
            {
                message.setSubject(String.valueOf(this.hostAlertSubject.get(context, notification)));
                message.setContent(String.valueOf(this.hostAlertContent.get(context, notification)), "text/plain");
            }
        }
        else if (notification instanceof SendRecovery)
        {
            logger.debug("Building recovery message content");
            if (notification.getCheck() instanceof ServiceMO)
            {
                message.setSubject(String.valueOf(this.serviceRecoverySubject.get(context, notification)));
                message.setContent(String.valueOf(this.serviceRecoveryContent.get(context, notification)), "text/plain");
            }
            else if (notification.getCheck() instanceof HostMO)
            {
                message.setSubject(String.valueOf(this.hostRecoverySubject.get(context, notification)));
                message.setContent(String.valueOf(this.hostRecoveryContent.get(context, notification)), "text/plain");
            }
        }
    }

    protected ExpressContext createContext(Notification notification)
    {
        ExpressContext ctx = new DefaultContext(this.expressExtensions, new ExpressEntityResolver()
        {
            @Override
            public Object getEntity(String name, Object source)
            {
                if (notification != null)
                {
                    if ("this".equals(name))
                    {
                        return notification;
                    }
                    else if ("check".equals(name))
                    {
                        return notification.getCheck(); 
                    }
                    else if ("service".equals(name) && (notification.getCheck() instanceof ServiceMO))
                    {
                        return notification.getCheck();
                    }
                    else if ("host".equals(name) && (notification.getCheck() instanceof HostMO))
                    {
                        return notification.getCheck();
                    }
                    else if ("host".equals(name) && (notification.getCheck() instanceof ServiceMO))
                    {
                        return ((ServiceMO) notification.getCheck()).getHost();
                    }
                }
                return null;
            }

            @Override
            public ActionHandler getAction(String name, Object source)
            {
                return null;
            }
        });
        return ctx;
    }

    public static final class Templates
    {
        public static final class Service
        {
            public static final class Alert
            {
                public static final String SUBJECT = "Alert for service #{service.displayName} on the host #{host.displayName} is #{service.state.status}";

                public static final String CONTENT = "Alert for service #{service.displayName} on the host #{host.displayName} is #{service.state.status}\r\n"
                                                   + "\r\n"
                                                   + "Check output: #{service.state.output}\r\n"
                                                   + "\r\n"
                                                   + "Last checked at #{dateformat('HH:mm:ss', service.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', service.state.lastCheckTime)}\r\n"
                                                   + "\r\n"
                                                   + "For more information: http://bergamot/service/id/#{service.id}\r\n"
                                                   + "\r\n"
                                                   + "Thank you, Bergamot";
            }
            
            public static final class Recovery
            {
                public static final String SUBJECT = "Recovery for service #{service.displayName} on the host #{host.displayName} is #{service.state.status}";

                public static final String CONTENT = "Recovery for service #{service.displayName} on the host #{host.displayName} is #{service.state.status}\r\n"
                                                   + "\r\n"
                                                   + "Check output: #{service.state.output}\r\n"
                                                   + "\r\n"
                                                   + "Last checked at #{dateformat('HH:mm:ss', service.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', service.state.lastCheckTime)}\r\n"
                                                   + "\r\n"
                                                   + "For more information: http://bergamot/service/id/#{service.id}\r\n"
                                                   + "\r\n"
                                                   + "Thank you, Bergamot";
            }
        }
        
        public static final class Host
        {
            public static final class Alert
            {
                public static final String SUBJECT = "Alert for host #{host.displayName} is #{if(host.state.ok, 'UP', 'DOWN')}";

                public static final String CONTENT = "Alert for service #{host.displayName} is #{if(host.state.ok, 'UP', 'DOWN')}\r\n"
                                                   + "\r\n"
                                                   + "Check output: #{host.state.output}\r\n"
                                                   + "\r\n"
                                                   + "Last checked at #{dateformat('HH:mm:ss', host.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', host.state.lastCheckTime)}\r\n"
                                                   + "\r\n"
                                                   + "For more information: http://bergamot/host/id/#{host.id}\r\n"
                                                   + "\r\n"
                                                   + "Thank you, Bergamot";
            }
            
            public static final class Recovery
            {
                public static final String SUBJECT = "Recovery for host #{host.displayName} is #{if(host.state.ok, 'UP', 'DOWN')}";

                public static final String CONTENT = "Recovery for host #{host.displayName} is #{if(host.state.ok, 'UP', 'DOWN')}\r\n"
                                                   + "\r\n"
                                                   + "Check output: #{host.state.output}\r\n"
                                                   + "\r\n"
                                                   + "Last checked at #{dateformat('HH:mm:ss', host.state.lastCheckTime)} on #{dateformat('EEEE dd/MM/yyyy', host.state.lastCheckTime)}\r\n"
                                                   + "\r\n"
                                                   + "For more information: http://bergamot/host/id/#{host.id}\r\n"
                                                   + "\r\n"
                                                   + "Thank you, Bergamot";
            }
        }
    }
}

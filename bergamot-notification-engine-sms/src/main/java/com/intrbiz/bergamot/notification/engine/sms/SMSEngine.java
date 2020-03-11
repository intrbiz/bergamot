package com.intrbiz.bergamot.notification.engine.sms;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.SendNotificationToContactAccountingEvent;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.engine.sms.io.AWSTransport;
import com.intrbiz.bergamot.notification.engine.sms.io.SMSTransport;
import com.intrbiz.bergamot.notification.engine.sms.io.SMSTransportException;
import com.intrbiz.bergamot.notification.engine.sms.io.TwilioTransport;
import com.intrbiz.bergamot.notification.engine.sms.model.SMSMessage;
import com.intrbiz.bergamot.notification.engine.sms.model.SentSMS;
import com.intrbiz.bergamot.notification.template.TemplatedNotificationEngine;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.QueueException;

public class SMSEngine extends TemplatedNotificationEngine
{
    public static final String NAME = "sms";

    private static final Logger logger = Logger.getLogger(SMSEngine.class);

    private String from;
    
    private final Timer smsSendTimer;
    
    private final Counter smsSendErrors;
    
    private Accounting accounting = Accounting.create(SMSEngine.class);
    
    private SMSTransport transport;

    public SMSEngine()
    {
        super(NAME);
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.sms");
        this.smsSendTimer = source.getRegistry().timer("sms-sent");
        this.smsSendErrors = source.getRegistry().counter("sms-errors");
    }
    
    protected SMSTransport loadTransport(String transportName) throws SMSTransportException
    {
        switch (transportName)
        {
            case "twilio":
                return new TwilioTransport();
            case "aws":
                return new AWSTransport();
        }
        throw new SMSTransportException("The transport: " + transportName + " is not known, cannot load transport");
    }

    @Override
    protected void doPrepare(NotificationEngineContext engineContext) throws Exception
    {
        super.doPrepare(engineContext);
        Configuration config = engineContext.getConfiguration();
        // from number
        this.from = config.getStringParameterValue("sms.from", "");
        logger.info("Sending SMS messages from: " + this.from);
        // load our SMS transport
        this.transport = this.loadTransport(config.getStringParameterValue("sms.transport", "twilio"));
        this.transport.configure(config);
        logger.info("Using " + this.transport + " to send SMS messages");
    }

    @Override
    public boolean accept(Notification notification)
    {
        Map<String, ContactMO> tos = this.getTo(notification);
        return ! tos.isEmpty();
    }

    @Override
    public void sendNotification(Notification notification)
    {
        logger.info("Sending SMS notification for " + notification.getNotificationType() + " to " + notification.getTo().stream().map(ContactMO::getPager).filter((e) -> { return e != null; }).collect(Collectors.toList()));
        Timer.Context tctx = this.smsSendTimer.time();
        try
        {
            try
            {
                Map<String, ContactMO> tos = this.getTo(notification);
                if (tos.isEmpty()) return;
                // build the message
                String message = this.buildMessage(notification);
                if (Util.isEmpty(message)) throw new RuntimeException("Failed to build message, not sending notifications");
                // send the SMSes
                for (Entry<String, ContactMO> to : tos.entrySet())
                {
                    try
                    {
                        SMSMessage sms = new SMSMessage(to.getKey(), this.from, message);
                        if (logger.isDebugEnabled()) logger.debug("Sending SMS: " + sms);
                        SentSMS sent = this.transport.sendSMS(sms);
                        if (logger.isDebugEnabled()) logger.debug("Sent SMS: " + sent);
                        // accounting
                        this.accounting.account(new SendNotificationToContactAccountingEvent(
                            notification.getSite().getId(),
                            notification.getId(),
                            getObjectId(notification),
                            getNotificationType(notification),
                            to.getValue().getId(),
                            this.getName(),
                            "sms",
                            sms.getTo(),
                            sent.getMessageId()
                        ));
                    }
                    catch (Exception e)
                    {
                        this.smsSendErrors.inc();
                        logger.error("Failed to send SMS notification to " + to.getKey() + " - " + to.getValue().getName(), e);
                    }
                }
            }
            catch (Exception e)
            {
                this.smsSendErrors.inc();
                logger.error("Failed to send SMS notification", e);
                throw new QueueException("Failed to send email notification", e);
            }
        }
        finally
        {
            tctx.stop();
        }
    }
    
    protected Map<String, ContactMO> getTo(Notification notification)
    {
        Map<String, ContactMO> to = new TreeMap<String, ContactMO>();
        for (ContactMO contact : notification.getTo())
        {
            if ((!Util.isEmpty(Util.coalesceEmpty(contact.getPager(), contact.getMobile()))) && contact.hasEngine(this.getName()))
            {
                to.put(Util.coalesceEmpty(contact.getPager(), contact.getMobile()), contact);
            }
        }
        return to;
    }

    protected String buildMessage(Notification notification) throws Exception
    {
        if (notification instanceof CheckNotification)
        {
            return this.applyTemplate(((CheckNotification)notification).getCheck().getCheckType() + "." + notification.getNotificationType() + ".message", notification);
        }
        else
        {
            return this.applyTemplate(notification.getNotificationType() + ".message", notification);
        }
    }
}

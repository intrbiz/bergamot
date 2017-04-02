package com.intrbiz.bergamot.notification.engine.sms.io;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.notification.engine.sms.model.SMSMessage;
import com.intrbiz.bergamot.notification.engine.sms.model.SentSMS;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;

public class TwilioTransport implements SMSTransport
{
    private static final Logger logger = Logger.getLogger(TwilioTransport.class);
    
    private String accountSid;

    private String authToken;
    
    private NotificationEngineCfg config;
    
    public TwilioTransport()
    {
        super();
    }

    @Override
    public void configure(NotificationEngineCfg cfg) throws Exception
    {
        this.config = cfg;
        // auth details
        this.accountSid = this.config.getStringParameterValue("twilio.account", "");
        this.authToken = this.config.getStringParameterValue("twilio.token", "");
        logger.info("Using the Twilio account: " + this.accountSid);
        // setup the client
        Twilio.init(this.accountSid, this.authToken);
    }

    @Override
    public NotificationEngineCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public SentSMS sendSMS(SMSMessage message) throws SMSTransportException
    {
        try
        {
            // Build the SMS
            MessageCreator messageCreator = Message.creator(
                    new PhoneNumber(message.getTo()),
                    new PhoneNumber(message.getFrom()),
                    message.getMessage()
            ); 
            // Send the SMS
            Message sent = messageCreator.create();
            logger.info("Sent SMS " + message + " ==> Id: " + sent.getSid() + ", Status:" + sent.getStatus());
            return new SentSMS(true, sent.getSid());
        }
        catch (Exception e)
        {
            throw new SMSTransportException("Failed to send SMS to " + message.getTo());
        }
    }
    
    public String toString()
    {
        return "Twilio";
    }
}

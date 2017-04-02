package com.intrbiz.bergamot.notification.engine.sms.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.notification.engine.sms.model.SMSMessage;
import com.intrbiz.bergamot.notification.engine.sms.model.SentSMS;

public class AWSTransport implements SMSTransport
{
    private static final Logger logger = Logger.getLogger(AWSTransport.class);
    
    private NotificationEngineCfg config;
    
    private BasicAWSCredentials awsCredentials;
    
    private AmazonSNS snsClient;
    
    public AWSTransport()
    {
        super();
    }

    @Override
    public void configure(NotificationEngineCfg cfg) throws Exception
    {
        this.config = cfg;
        // auth details
        this.awsCredentials = new BasicAWSCredentials(cfg.getStringParameterValue("aws.accessKeyId", null), cfg.getStringParameterValue("aws.secretKey", null));
        logger.info("Using the Amazon Web Services account: " + this.awsCredentials.getAWSAccessKeyId());
        // setup the client
        this.snsClient = AmazonSNSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(this.awsCredentials))
                .withRegion(cfg.getStringParameterValue("aws.region", "eu-west-1"))
                .build();
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
            // AWS SMS attributes
            Map<String, MessageAttributeValue> smsAttrs = new HashMap<String, MessageAttributeValue>();
            // set the SenderId to the from info
            smsAttrs.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                    .withStringValue(message.getFrom()).withDataType("String"));
            // these SMS messages are important to transactional
            smsAttrs.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                    .withStringValue("Transactional").withDataType("String"));
            // send the SMS
            PublishResult result = this.snsClient.publish(new PublishRequest()
                    .withPhoneNumber(message.getTo())
                    .withMessage(message.getMessage())
                    .withMessageAttributes(smsAttrs));
            // return the sent message info
            return new SentSMS(true, result.getMessageId());
        }
        catch (Exception e)
        {
            throw new SMSTransportException("Failed to send SMS to " + message.getTo());
        }
    }
    
    public String toString()
    {
        return "Amazon Web Services - SNS";
    }
}

package com.intrbiz.bergamot.notification.engine.sms.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.intrbiz.Util;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.engine.sms.model.SMSMessage;
import com.intrbiz.bergamot.notification.engine.sms.model.SentSMS;

public class AWSTransport implements SMSTransport
{
    private static final Logger logger = Logger.getLogger(AWSTransport.class);
    
    private AWSCredentialsProvider awsCredentials;
    
    private AmazonSNS snsClient;
    
    public AWSTransport()
    {
        super();
    }

    @Override
    public void configure(NotificationEngineContext engineContext) throws Exception
    {
        // auth details
        String awsAccessKeyId = engineContext.getParameter("aws.accessKeyId", null);
        String awsSecretKey   = engineContext.getParameter("aws.secretKey", null);
        if (Util.isEmpty(awsAccessKeyId) || Util.isEmpty(awsSecretKey))
        {
            this.awsCredentials = new EC2ContainerCredentialsProviderWrapper();
            logger.info("Using Amazon Web Services instance or container provided credentials");
        }
        else
        {
            this.awsCredentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretKey));
            logger.info("Using the Amazon Web Services account: " + awsAccessKeyId);
        }
        // setup the client
        this.snsClient = AmazonSNSClientBuilder.standard()
                .withCredentials(this.awsCredentials)
                .withRegion(engineContext.getParameter("aws.region", "eu-west-1"))
                .build();
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

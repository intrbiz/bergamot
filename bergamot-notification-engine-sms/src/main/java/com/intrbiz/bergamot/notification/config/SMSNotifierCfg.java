package com.intrbiz.bergamot.notification.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.bergamot.notification.engine.sms.SMSEngine;
import com.intrbiz.configuration.CfgParameter;

@XmlType(name = "notifier")
@XmlRootElement(name = "notifier")
public class SMSNotifierCfg extends NotifierCfg
{
    private static final long serialVersionUID = 1L;

    @Override
    public void applyDefaults()
    {
        super.applyDefaults();
        // the engines
        if (this.getEngines().isEmpty())
        {
            // email engine with default templates
            this.getEngines().add(
                    new NotificationEngineCfg(
                            SMSEngine.class,
                            new CfgParameter("transport", "The transport used to send SMS messages, choice of: [twilio, aws]", "twilio"),
                            new CfgParameter("twilio.account", "The Twilio account SID", ""), 
                            new CfgParameter("twilio.token", "The Twilio auth token", ""),
                            new CfgParameter("aws.accessKeyId", "The AWS access key id", ""), 
                            new CfgParameter("aws.secretKey", "The AWS secret key", ""),
                            new CfgParameter("aws.region", "The AWS Region to use", "eu-west-1"),
                            new CfgParameter("from", "The from phone number", "+440000000000") 
                    )
            );
        }

    }
}

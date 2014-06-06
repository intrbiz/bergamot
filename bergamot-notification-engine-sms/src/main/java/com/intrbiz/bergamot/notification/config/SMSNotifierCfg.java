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
                            new CfgParameter("twillo.account", "The Twillo account SID", ""), 
                            new CfgParameter("twillo.token", "The Twillo auth token", ""), 
                            new CfgParameter("from", "The from phone number", "+440000000000") 
                    )
            );
        }

    }
}

package com.intrbiz.bergamot.notification.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.bergamot.notification.engine.email.EmailEngine;
import com.intrbiz.configuration.CfgParameter;

@XmlType(name = "notifier")
@XmlRootElement(name = "notifier")
public class EmailNotifierCfg extends NotifierCfg
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
                            EmailEngine.class, 
                            new CfgParameter("mail.host", "127.0.0.1"), 
                            new CfgParameter("mail.user", ""), 
                            new CfgParameter("mail.password", ""), 
                            new CfgParameter("from", "bergamot@localhost")
                    )
            );
        }

    }
}

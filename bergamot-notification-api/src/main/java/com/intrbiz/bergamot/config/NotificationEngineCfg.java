package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.notification.NotificationEngine;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "notification-engine")
@XmlRootElement(name = "notification-engine")
public class NotificationEngineCfg extends Configuration
{
    public NotificationEngineCfg()
    {
        super();
    }
    
    public NotificationEngineCfg(String engineCls, CfgParameter... parameters)
    {
        super();
        this.setClassname(engineCls);
        for (CfgParameter parameter : parameters)
        {
            this.addParameter(parameter);
        }
    }
    
    public NotificationEngineCfg(Class<? extends NotificationEngine> engineCls, CfgParameter... parameters)
    {
        this(engineCls.getCanonicalName(), parameters);
    }
}

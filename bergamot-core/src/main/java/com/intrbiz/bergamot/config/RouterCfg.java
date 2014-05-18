package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name = "router")
@XmlRootElement(name = "router")
public class RouterCfg extends Configuration
{
    public RouterCfg()
    {
        super();
    }
    
    public RouterCfg(String routerClass)
    {
        super();
        this.setClassname(routerClass);
    }
}

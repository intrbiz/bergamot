package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name = "scheduler")
@XmlRootElement(name = "scheduler")
public class SchedulerCfg extends Configuration
{
    public SchedulerCfg()
    {
        super();
    }
}

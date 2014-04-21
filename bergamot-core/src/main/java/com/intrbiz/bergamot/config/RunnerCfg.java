package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.worker.Runner;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "runner")
@XmlRootElement(name = "runner")
public class RunnerCfg extends Configuration
{
    public RunnerCfg()
    {
        super();
    }
    
    public RunnerCfg(Class<? extends Runner> runnerClass)
    {
        super();
        this.setClassname(runnerClass.getName());
    }
}

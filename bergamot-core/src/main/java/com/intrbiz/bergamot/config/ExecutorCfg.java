package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.worker.Executor;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "executor")
@XmlRootElement(name = "executor")
public class ExecutorCfg extends Configuration
{
    public ExecutorCfg()
    {
        super();
    }
    
    public ExecutorCfg(Class<? extends Executor<?>> runnerClass)
    {
        super();
        this.setClassname(runnerClass.getName());
    }
}

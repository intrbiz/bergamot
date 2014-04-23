package com.intrbiz.bergamot.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name = "engine")
@XmlRootElement(name = "engine")
public class EngineCfg extends Configuration
{
    private List<ExecutorCfg> executors = new LinkedList<ExecutorCfg>();
    
    public EngineCfg()
    {
        super();
    }
    
    public EngineCfg(Class<?> engine, ExecutorCfg... runners)
    {
        super();
        this.setClassname(engine.getName());
        for (ExecutorCfg runner : runners)
        {
            this.executors.add(runner);
        }
    }
    
    @XmlElementRef(type = ExecutorCfg.class)
    public List<ExecutorCfg> getExecutors()
    {
        return executors;
    }

    public void setExecutors(List<ExecutorCfg> executors)
    {
        this.executors = executors;
    }
    
    @Override
    public void applyDefaults()
    {
        for (ExecutorCfg executor : this.executors)
        {
            executor.applyDefaults();
        }
    }
}

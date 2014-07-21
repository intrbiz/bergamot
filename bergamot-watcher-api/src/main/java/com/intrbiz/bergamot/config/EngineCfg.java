package com.intrbiz.bergamot.config;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.watcher.engine.Engine;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "engine")
@XmlRootElement(name = "engine")
public class EngineCfg extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    private List<ExecutorCfg> executors = new LinkedList<ExecutorCfg>();
    
    public EngineCfg()
    {
        super();
    }
    
    public EngineCfg(String engine, ExecutorCfg... executors)
    {
        super();
        this.setClassname(engine);
        for (ExecutorCfg listener : executors)
        {
            this.executors.add(listener);
        }
    }
    
    public EngineCfg(Class<? extends Engine> engine, ExecutorCfg... executors)
    {
        this(engine.getCanonicalName(), executors);
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

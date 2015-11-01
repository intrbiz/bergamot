package com.intrbiz.bergamot.worker.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.jmx.JMXEngine;
import com.intrbiz.bergamot.worker.engine.jmx.ScriptedJMXExecutor;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class JMXWorkerCfg extends WorkerCfg
{
    private static final long serialVersionUID = 1L;

    public JMXWorkerCfg()
    {
        super();
    }
    
    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(
             new EngineCfg(
                 JMXEngine.class,  
                 new ExecutorCfg(ScriptedJMXExecutor.class)
             )
        );
        // apply defaults from super class
        super.applyDefaults();
    }
}

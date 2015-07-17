package com.intrbiz.bergamot.worker.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.jdbc.JDBCEngine;
import com.intrbiz.bergamot.worker.engine.jdbc.ScriptedJDBCExecutor;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class JDBCWorkerCfg extends WorkerCfg
{
    private static final long serialVersionUID = 1L;

    public JDBCWorkerCfg()
    {
        super();
    }
    
    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(
             new EngineCfg(
                 JDBCEngine.class,  
                 new ExecutorCfg(ScriptedJDBCExecutor.class)
             )
        );
        // apply defaults from super class
        super.applyDefaults();
    }
}

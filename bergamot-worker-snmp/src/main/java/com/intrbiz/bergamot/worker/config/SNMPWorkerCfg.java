package com.intrbiz.bergamot.worker.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.snmp.GetSNMPExecutor;
import com.intrbiz.bergamot.worker.engine.snmp.SNMPEngine;
import com.intrbiz.bergamot.worker.engine.snmp.ScriptedSNMPExecutor;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class SNMPWorkerCfg extends WorkerCfg
{
    private static final long serialVersionUID = 1L;

    public SNMPWorkerCfg()
    {
        super();
    }
    
    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(
                new EngineCfg(
                        SNMPEngine.class, 
                        new ExecutorCfg(ScriptedSNMPExecutor.class),
                        new ExecutorCfg(GetSNMPExecutor.class)
                )
        );
        // apply defaults from super class
        super.applyDefaults();
    }
}

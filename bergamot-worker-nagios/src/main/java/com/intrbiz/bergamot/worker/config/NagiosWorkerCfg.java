package com.intrbiz.bergamot.worker.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.nagios.NagiosEngine;
import com.intrbiz.bergamot.worker.engine.nagios.NagiosExecutor;
import com.intrbiz.bergamot.worker.engine.nrpe.NRPEEngine;
import com.intrbiz.bergamot.worker.engine.nrpe.NRPEExecutor;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class NagiosWorkerCfg extends WorkerCfg
{
    public NagiosWorkerCfg()
    {
        super();
    }
    
    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(new EngineCfg(NagiosEngine.class, new ExecutorCfg(NagiosExecutor.class)));
        this.getEngines().add(new EngineCfg(NRPEEngine.class, new ExecutorCfg(NRPEExecutor.class)));
        // apply defaults from super class
        super.applyDefaults();
    }
}

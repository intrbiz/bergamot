package com.intrbiz.bergamot.worker.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.dummy.DummyEngine;
import com.intrbiz.bergamot.worker.engine.dummy.RandomExecutor;
import com.intrbiz.bergamot.worker.engine.dummy.StaticExecutor;
import com.intrbiz.bergamot.worker.engine.dummy.TimedExecutor;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class DummyWorkerCfg extends WorkerCfg
{
    private static final long serialVersionUID = 1L;

    public DummyWorkerCfg()
    {
        super();
    }

    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(
                new EngineCfg(DummyEngine.class, 
                        new ExecutorCfg(StaticExecutor.class),
                        new ExecutorCfg(RandomExecutor.class),
                        new ExecutorCfg(TimedExecutor.class)
                ));
        // apply defaults from super class
        super.applyDefaults();
    }
}

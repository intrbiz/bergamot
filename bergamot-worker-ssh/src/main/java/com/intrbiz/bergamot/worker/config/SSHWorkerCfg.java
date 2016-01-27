package com.intrbiz.bergamot.worker.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.ssh.NagiosSSHExecutor;
import com.intrbiz.bergamot.worker.engine.ssh.SSHEngine;
import com.intrbiz.bergamot.worker.engine.ssh.ScriptedSSHExecutor;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class SSHWorkerCfg extends WorkerCfg
{
    private static final long serialVersionUID = 1L;

    public SSHWorkerCfg()
    {
        super();
    }
    
    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(new EngineCfg(SSHEngine.class, new ExecutorCfg(NagiosSSHExecutor.class), new ExecutorCfg(ScriptedSSHExecutor.class)));
        // apply defaults from super class
        super.applyDefaults();
    }
}

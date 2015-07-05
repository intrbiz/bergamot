package com.intrbiz.bergamot.worker.config;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.agent.server.config.BergamotAgentServerCfg;
import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.agent.AgentEngine;
import com.intrbiz.bergamot.worker.engine.agent.AgentExecutor;
import com.intrbiz.bergamot.worker.engine.agent.AgentMemoryExecutor;
import com.intrbiz.bergamot.worker.engine.agent.CPUExecutor;
import com.intrbiz.bergamot.worker.engine.agent.DiskExecutor;
import com.intrbiz.bergamot.worker.engine.agent.DiskIOExecutor;
import com.intrbiz.bergamot.worker.engine.agent.DisksExecutor;
import com.intrbiz.bergamot.worker.engine.agent.LoadExecutor;
import com.intrbiz.bergamot.worker.engine.agent.MemoryExecutor;
import com.intrbiz.bergamot.worker.engine.agent.NagiosExecutor;
import com.intrbiz.bergamot.worker.engine.agent.NetConExecutor;
import com.intrbiz.bergamot.worker.engine.agent.NetIOExecutor;
import com.intrbiz.bergamot.worker.engine.agent.OSExecutor;
import com.intrbiz.bergamot.worker.engine.agent.PortListenerExecutor;
import com.intrbiz.bergamot.worker.engine.agent.PresenceExecutor;
import com.intrbiz.bergamot.worker.engine.agent.ProcessStatsExecutor;
import com.intrbiz.bergamot.worker.engine.agent.ProcessesExecutor;
import com.intrbiz.bergamot.worker.engine.agent.UptimeExecutor;
import com.intrbiz.bergamot.worker.engine.agent.UsersExecutor;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class AgentWorkerCfg extends WorkerCfg
{
    private static final long serialVersionUID = 1L;

    private BergamotAgentServerCfg agentServer;

    public AgentWorkerCfg()
    {
        super();
    }

    @XmlElementRef(type = BergamotAgentServerCfg.class)
    public BergamotAgentServerCfg getAgentServer()
    {
        return agentServer;
    }

    public void setAgentServer(BergamotAgentServerCfg agentServer)
    {
        this.agentServer = agentServer;
    }

    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(
                new EngineCfg(AgentEngine.class, 
                        new ExecutorCfg(PresenceExecutor.class), 
                        new ExecutorCfg(CPUExecutor.class), 
                        new ExecutorCfg(MemoryExecutor.class), 
                        new ExecutorCfg(DiskExecutor.class),
                        new ExecutorCfg(DisksExecutor.class),
                        new ExecutorCfg(OSExecutor.class),
                        new ExecutorCfg(UptimeExecutor.class),
                        new ExecutorCfg(NagiosExecutor.class),
                        new ExecutorCfg(UsersExecutor.class),
                        new ExecutorCfg(ProcessesExecutor.class),
                        new ExecutorCfg(ProcessStatsExecutor.class),
                        new ExecutorCfg(AgentExecutor.class),
                        new ExecutorCfg(AgentMemoryExecutor.class),
                        new ExecutorCfg(NetConExecutor.class),
                        new ExecutorCfg(PortListenerExecutor.class),
                        new ExecutorCfg(NetIOExecutor.class),
                        new ExecutorCfg(DiskIOExecutor.class),
                        new ExecutorCfg(LoadExecutor.class)
                ));
        // apply defaults from super class
        super.applyDefaults();
    }
}

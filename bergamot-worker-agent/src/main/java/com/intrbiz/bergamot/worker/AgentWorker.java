package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.worker.engine.AbstractEngine;
import com.intrbiz.bergamot.worker.engine.agent.AgentExecutor;
import com.intrbiz.bergamot.worker.engine.agent.AgentMemoryExecutor;
import com.intrbiz.bergamot.worker.engine.agent.CPUExecutor;
import com.intrbiz.bergamot.worker.engine.agent.DiskExecutor;
import com.intrbiz.bergamot.worker.engine.agent.DiskIOExecutor;
import com.intrbiz.bergamot.worker.engine.agent.DisksExecutor;
import com.intrbiz.bergamot.worker.engine.agent.LoadExecutor;
import com.intrbiz.bergamot.worker.engine.agent.MemoryExecutor;
import com.intrbiz.bergamot.worker.engine.agent.MetricsExecutor;
import com.intrbiz.bergamot.worker.engine.agent.NagiosExecutor;
import com.intrbiz.bergamot.worker.engine.agent.NetConExecutor;
import com.intrbiz.bergamot.worker.engine.agent.NetIOExecutor;
import com.intrbiz.bergamot.worker.engine.agent.OSExecutor;
import com.intrbiz.bergamot.worker.engine.agent.PortListenerExecutor;
import com.intrbiz.bergamot.worker.engine.agent.PresenceExecutor;
import com.intrbiz.bergamot.worker.engine.agent.ProcessStatsExecutor;
import com.intrbiz.bergamot.worker.engine.agent.ProcessesExecutor;
import com.intrbiz.bergamot.worker.engine.agent.ScriptExecutor;
import com.intrbiz.bergamot.worker.engine.agent.UptimeExecutor;
import com.intrbiz.bergamot.worker.engine.agent.UsersExecutor;
 
/**
 * A worker to execute SNMP check engines
 */
public class AgentWorker extends AbstractEngine
{
    public static final String NAME = "agent";
    
    public AgentWorker()
    {
        super(NAME,
                new PresenceExecutor(), 
                new CPUExecutor(), 
                new MemoryExecutor(), 
                new DiskExecutor(),
                new DisksExecutor(),
                new OSExecutor(),
                new UptimeExecutor(),
                new NagiosExecutor(),
                new UsersExecutor(),
                new ProcessesExecutor(),
                new ProcessStatsExecutor(),
                new AgentExecutor(),
                new AgentMemoryExecutor(),
                new NetConExecutor(),
                new PortListenerExecutor(),
                new NetIOExecutor(),
                new DiskIOExecutor(),
                new LoadExecutor(),
                new MetricsExecutor(),
                new ScriptExecutor()
        );
    }
}

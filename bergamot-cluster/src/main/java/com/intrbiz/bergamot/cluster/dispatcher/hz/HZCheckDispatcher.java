package com.intrbiz.bergamot.cluster.dispatcher.hz;

import java.util.Objects;
import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.dispatcher.CheckDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

/**
 * Dispatch checks to workers
 */
public class HZCheckDispatcher extends HZBaseDispatcher<ExecuteCheck> implements CheckDispatcher
{
    private static final String ENGINE_AGENT = "agent";
    
    private final WorkerRegistry workers;
    
    private final WorkerRouteTable workerRouteTable;
    
    private final AgentRegistry agents;
    
    public HZCheckDispatcher(WorkerRegistry workers, AgentRegistry agents, HazelcastInstance hazelcast)
    {
        super(hazelcast, HZNames::buildWorkerRingbufferName);
        this.workers = Objects.requireNonNull(workers);
        this.workerRouteTable = Objects.requireNonNull(this.workers.getRouteTable());
        this.agents = Objects.requireNonNull(agents);
    }
    
    public PublishStatus dispatchCheck(ExecuteCheck check)
    {
        // Route the check
        UUID workerId = null;
        if (this.isAgentRouted(check))
        {
            if (check.getAgentId() == null)
            {
                return PublishStatus.NoAgentId;
            }
            else
            {
                workerId = this.agents.routeAgent(check.getAgentId());
                if (workerId == null) return PublishStatus.AgentUnroutable;
            }
        }
        else
        {
            workerId = this.workerRouteTable.route(check.getSiteId(), check.getWorkerPool(), check.getEngine());
            if (workerId == null) return PublishStatus.Unroutable;
        }
        // Offer to the worker
        return this.offer(workerId, check);
    }
    
    protected boolean isAgentRouted(ExecuteCheck check)
    {
        return check.getEngine() != null && check.getEngine().toLowerCase().startsWith(ENGINE_AGENT);
    }
}

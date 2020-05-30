package com.intrbiz.bergamot.cluster.dispatcher.hz;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.dispatcher.WorkerDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

/**
 * Dispatch checks to workers
 */
public class HZWorkerDispatcher extends HZBaseDispatcher<WorkerMessage> implements WorkerDispatcher
{
    private static final String ENGINE_AGENT = "agent";
    
    private static final String ENGINE_INTERNAL = "internal";
    
    private final WorkerRegistry workers;
    
    private final WorkerRouteTable workerRouteTable;
    
    private final AgentRegistry agents;
    
    private final ConcurrentMap<String, Consumer<WorkerMessage>> internalWorkers = new ConcurrentHashMap<>();
    
    public HZWorkerDispatcher(WorkerRegistry workers, AgentRegistry agents, HazelcastInstance hazelcast)
    {
        super(hazelcast, HZNames::buildWorkerRingbufferName);
        this.workers = Objects.requireNonNull(workers);
        this.workerRouteTable = Objects.requireNonNull(this.workers.getRouteTable());
        this.agents = Objects.requireNonNull(agents);
    }
    
    @Override
    public PublishStatus dispatchCheck(ExecuteCheck check)
    {
        return this.dispatch(check);
    }

    @Override
    public PublishStatus dispatch(WorkerMessage message)
    {
        if (this.isInternal(message))
        {
            Consumer<WorkerMessage> internal = this.internalWorkers.get(message.getEngine());
            if (internal != null)
            {
                internal.accept(message);
                return PublishStatus.Success;
            }
            return PublishStatus.Unroutable;
        }
        else
        {
            // Route the check
            UUID workerId = message.getWorkerId();
            if (workerId == null)
            {
                    if (this.isAgentRouted(message))
                    {
                        if (message.getAgentId() == null)
                        {
                            return PublishStatus.NoAgentId;
                        }
                        else
                        {
                            workerId = this.agents.routeAgent(message.getSiteId(), message.getAgentId());
                            if (workerId == null) return PublishStatus.AgentUnroutable;
                        }
                    }
                    else
                    {
                        workerId = this.workerRouteTable.route(message.getSiteId(), message.getWorkerPool(), message.getEngine());
                    }
                if (workerId == null) return PublishStatus.Unroutable;
                message.setWorkerId(workerId);
            }
            // Offer to the worker
            return this.offer(workerId, message);
        }
    }
    
    protected final boolean isAgentRouted(WorkerMessage check)
    {
        return check.getEngine() != null && check.getEngine().toLowerCase().startsWith(ENGINE_AGENT);
    }
    
    protected final boolean isInternal(WorkerMessage check)
    {
        return check.getEngine() != null && check.getEngine().toLowerCase().startsWith(ENGINE_INTERNAL);
    }
    
    public void registerInternalWorker(String engineName, Consumer<WorkerMessage> consumer)
    {
        if (Objects.requireNonNull(engineName).startsWith(ENGINE_INTERNAL))
        {
            this.internalWorkers.put(engineName, consumer);
        }
    }
}

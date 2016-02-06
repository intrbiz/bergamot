package com.intrbiz.bergamot.model.message.api.check;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

@JsonTypeName("bergamot.api.execute_adhoc_check")
public class ExecuteAdhocCheck extends APIRequest
{
    @JsonProperty("check")
    private ExecuteCheck check;
    
    @JsonProperty("worker_pool")
    private String workerPool;
    
    @JsonProperty("engine")
    private String engine;
    
    @JsonProperty("agent_id")
    private UUID agentId;
    
    @JsonProperty("ttl")
    private long ttl;
    
    public ExecuteAdhocCheck()
    {
        super();
    }
    
    public ExecuteAdhocCheck(ExecuteCheck check)
    {
        super();
        this.check = check;
    }

    public ExecuteCheck getCheck()
    {
        return check;
    }

    public void setCheck(ExecuteCheck check)
    {
        this.check = check;
    }

    public String getWorkerPool()
    {
        return workerPool;
    }

    public void setWorkerPool(String workerPool)
    {
        this.workerPool = workerPool;
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    public long getTtl()
    {
        return ttl;
    }

    public void setTtl(long ttl)
    {
        this.ttl = ttl;
    }
}

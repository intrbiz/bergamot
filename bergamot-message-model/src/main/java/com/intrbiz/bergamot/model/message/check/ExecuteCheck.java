package com.intrbiz.bergamot.model.message.check;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.ParameterisedMO;

/**
 * Execute this check please
 */
@JsonTypeName("bergamot.execute_check")
public class ExecuteCheck extends Message implements ParameterisedMO
{
    @JsonProperty("engine")
    private String engine;

    @JsonProperty("executor")
    private String executor;

    @JsonProperty("name")
    private String name;

    @JsonProperty("check_type")
    private String checkType;

    @JsonProperty("check_id")
    private UUID checkId;

    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("processing_pool")
    private int processingPool;

    @JsonProperty("parameters")
    private List<ParameterMO> parameters = new LinkedList<ParameterMO>();

    @JsonProperty("timeout")
    private long timeout = 30_000L;

    @JsonProperty("scheduled")
    private long scheduled;

    public ExecuteCheck()
    {
        super();
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public String getCheckType()
    {
        return checkType;
    }

    public void setCheckType(String checkType)
    {
        this.checkType = checkType;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    @Override
    public List<ParameterMO> getParameters()
    {
        return parameters;
    }

    @Override
    public void setParameters(List<ParameterMO> parameters)
    {
        this.parameters = parameters;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getScheduled()
    {
        return scheduled;
    }

    public void setScheduled(long scheduled)
    {
        this.scheduled = scheduled;
    }

    public int getProcessingPool()
    {
        return processingPool;
    }

    public void setProcessingPool(int processingPool)
    {
        this.processingPool = processingPool;
    }
}

package com.intrbiz.bergamot.model.message.check;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.ParameterisedMO;

public class CheckMessage extends Message implements ParameterisedMO
{
    private static final long serialVersionUID = 1L;

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
    
    @JsonProperty("worker_pool")
    private String workerPool;
    
    @JsonProperty("processor")
    private UUID processor;

    @JsonProperty("parameters")
    private List<ParameterMO> parameters = new LinkedList<ParameterMO>(); 
    
    /**
     * An id added to adhoc checks to correlate 
     * them with with the originator.  This must 
     * be null for normal check executions
     */
    @JsonProperty("adhoc_id")
    private UUID adhocId;
    
    public CheckMessage()
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

    public UUID getProcessor()
    {
        return this.processor;
    }

    public void setProcessor(UUID processor)
    {
        this.processor = processor;
    }

    public String getWorkerPool()
    {
        return workerPool;
    }

    public void setWorkerPool(String workerPool)
    {
        this.workerPool = workerPool;
    }

    public UUID getAdhocId()
    {
        return adhocId;
    }

    public void setAdhocId(UUID adhocId)
    {
        this.adhocId = adhocId;
    }
}

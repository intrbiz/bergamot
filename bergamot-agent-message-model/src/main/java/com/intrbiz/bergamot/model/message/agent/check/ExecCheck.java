package com.intrbiz.bergamot.model.message.agent.check;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;

/**
 * Execute a server defined check
 */
@JsonTypeName("bergamot.agent.check.exec")
public class ExecCheck extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("engine")
    private String engine;

    @JsonProperty("executor")
    private String executor;

    @JsonProperty("name")
    private String name;

    @JsonProperty("parameters")
    private List<ParameterMO> parameters = new LinkedList<ParameterMO>();
    
    public ExecCheck()
    {
        super();
    }

    public ExecCheck(String engine, String executor, String name, List<ParameterMO> parameters)
    {
        super();
        this.engine = engine;
        this.executor = executor;
        this.name = name;
        this.parameters = parameters;
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<ParameterMO> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<ParameterMO> parameters)
    {
        this.parameters = parameters;
    }
}

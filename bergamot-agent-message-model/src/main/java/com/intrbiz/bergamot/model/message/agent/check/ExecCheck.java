package com.intrbiz.bergamot.model.message.agent.check;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.util.Parameter;

/**
 * Execute a server defined check
 */
@JsonTypeName("bergamot.agent.check.exec")
public class ExecCheck extends AgentMessage
{
    @JsonProperty("engine")
    private String engine;

    @JsonProperty("executor")
    private String executor;

    @JsonProperty("name")
    private String name;

    @JsonProperty("parameters")
    private List<Parameter> parameters = new LinkedList<Parameter>();
    
    public ExecCheck()
    {
        super();
    }

    public ExecCheck(AgentMessage message)
    {
        super(message);
    }

    public ExecCheck(String id)
    {
        super(id);
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

    public List<Parameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters)
    {
        this.parameters = parameters;
    }
}

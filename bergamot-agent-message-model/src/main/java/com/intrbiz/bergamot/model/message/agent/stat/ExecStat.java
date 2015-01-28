package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.util.Parameter;
import com.intrbiz.bergamot.model.message.agent.util.Parameterised;

/**
 * Result of executing a server defined check
 */
@JsonTypeName("bergamot.agent.stat.exec")
public class ExecStat extends AgentMessage implements Parameterised
{
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("status")
    private String status;

    @JsonProperty("output")
    private String output;
    
    @JsonProperty("runtime")
    private double runtime;

    @JsonProperty("parameters")
    private List<Parameter> parameters = new LinkedList<Parameter>();

    public ExecStat()
    {
        super();
    }

    public ExecStat(AgentMessage message)
    {
        super(message);
    }

    public ExecStat(String id)
    {
        super(id);
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public double getRuntime()
    {
        return runtime;
    }

    public void setRuntime(double runtime)
    {
        this.runtime = runtime;
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

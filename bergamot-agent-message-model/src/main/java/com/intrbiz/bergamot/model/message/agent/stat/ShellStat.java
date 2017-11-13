package com.intrbiz.bergamot.model.message.agent.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

/**
 * Result of executing a shell command on a server
 */
@JsonTypeName("bergamot.agent.stat.shell")
public class ShellStat extends AgentMessage
{
    @JsonProperty("exit")
    private int exit;

    @JsonProperty("output")
    private String output;
    
    @JsonProperty("runtime")
    private double runtime;

    public ShellStat()
    {
        super();
    }

    public ShellStat(AgentMessage message)
    {
        super(message);
    }

    public ShellStat(String id)
    {
        super(id);
    }

    public int getExit()
    {
        return exit;
    }

    public void setExit(int exit)
    {
        this.exit = exit;
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
}

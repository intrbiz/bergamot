package com.intrbiz.bergamot.model.message.agent.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

/**
 * Result of executing a shell command on a server
 */
@JsonTypeName("bergamot.agent.stat.shell")
public class ShellStat extends Message
{
    private static final long serialVersionUID = 1L;
    
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

    public ShellStat(Message message)
    {
        super(message);
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

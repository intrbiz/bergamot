package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

/**
 * Execute a shell command on the server
 */
@JsonTypeName("bergamot.agent.check.shell")
public class ShellCheck extends AgentMessage
{
    @JsonProperty("command-line")
    private String commandLine;
    
    @JsonProperty("run-as")
    private String runAs;
    
    public ShellCheck()
    {
        super();
    }

    public ShellCheck(String commandLine, String runAs)
    {
        super();
        this.commandLine = commandLine;
        this.runAs = runAs;
    }

    public ShellCheck(AgentMessage message)
    {
        super(message);
    }

    public ShellCheck(String id)
    {
        super(id);
    }

    public String getCommandLine()
    {
        return commandLine;
    }

    public void setCommandLine(String commandLine)
    {
        this.commandLine = commandLine;
    }

    public String getRunAs()
    {
        return runAs;
    }

    public void setRunAs(String runAs)
    {
        this.runAs = runAs;
    }
}

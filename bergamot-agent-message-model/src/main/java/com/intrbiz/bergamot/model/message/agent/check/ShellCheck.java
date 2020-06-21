package com.intrbiz.bergamot.model.message.agent.check;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

/**
 * Execute a shell command on the server
 */
@JsonTypeName("bergamot.agent.check.shell")
public class ShellCheck extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("command-line")
    private String commandLine;
    
    @JsonProperty("environment")
    private Map<String, String> environment = new HashMap<String, String>();
    
    public ShellCheck()
    {
        super();
    }

    public ShellCheck(String commandLine, Map<String, String> environment)
    {
        super();
        this.commandLine = commandLine;
        this.environment = environment;
    }

    public String getCommandLine()
    {
        return commandLine;
    }

    public void setCommandLine(String commandLine)
    {
        this.commandLine = commandLine;
    }

    public Map<String, String> getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment)
    {
        this.environment = environment;
    }
}

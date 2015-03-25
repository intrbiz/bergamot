package com.intrbiz.bergamot.model.message.agent.check;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.process")
public class CheckProcess extends AgentMessage
{   
    /**
     * Send back a process list (default: yes)
     */
    @JsonProperty("list_processes")
    private boolean listProcesses = true;
    
    /**
     * Optional command name to filter on
     */
    @JsonProperty("command")
    private String command;
    
    /**
     * Flatten the command line and match against that?
     */
    private boolean flattenCommand = false;
    
    /**
     * Regex match the filters
     */
    @JsonProperty("regex")
    private boolean regex = false;
    
    /**
     * Optional process state to filter on
     */
    @JsonProperty("state")
    private List<String> state = new LinkedList<String>();
    
    /**
     * Optional user to filter on
     */
    @JsonProperty("user")
    private String user;
    
    /**
     * Optional group to filter on
     */
    @JsonProperty("group")
    private String group;
    
    public CheckProcess()
    {
        super();
    }

    public CheckProcess(AgentMessage message)
    {
        super(message);
    }

    public CheckProcess(String id)
    {
        super(id);
    }

    public boolean isListProcesses()
    {
        return listProcesses;
    }

    public void setListProcesses(boolean listProcesses)
    {
        this.listProcesses = listProcesses;
    }

    public List<String> getState()
    {
        return state;
    }

    public void setState(List<String> state)
    {
        this.state = state;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    public boolean isFlattenCommand()
    {
        return flattenCommand;
    }

    public void setFlattenCommand(boolean flattenCommand)
    {
        this.flattenCommand = flattenCommand;
    }

    public boolean isRegex()
    {
        return regex;
    }

    public void setRegex(boolean regex)
    {
        this.regex = regex;
    }
}

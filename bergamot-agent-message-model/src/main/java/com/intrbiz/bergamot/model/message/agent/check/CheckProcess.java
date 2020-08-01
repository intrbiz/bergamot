package com.intrbiz.bergamot.model.message.agent.check;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.process")
public class CheckProcess extends Message
{
    private static final long serialVersionUID = 1L;
    
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
    @JsonProperty("flatten_command")
    private boolean flattenCommand = false;
    
    /**
     * Optional arguments to filter on
     */
    @JsonProperty("arguments")
    private List<String> arguments = new LinkedList<String>();
    
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
    
    /**
     * Optional process title filter on
     */
    @JsonProperty("title")
    private String title;
    
    public CheckProcess()
    {
        super();
    }
    
    public CheckProcess(boolean listProcesses, String command, boolean flattenCommand, List<String> arguments, boolean regex, List<String> state, String user, String group, String title)
    {
        super();
        this.listProcesses = listProcesses;
        this.command = command;
        this.flattenCommand = flattenCommand;
        this.arguments = arguments;
        this.regex = regex;
        this.state = state;
        this.user = user;
        this.group = group;
        this.title = title;
    }

    public CheckProcess(Message message)
    {
        super(message);
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

    public List<String> getArguments()
    {
        return arguments;
    }

    public void setArguments(List<String> arguments)
    {
        this.arguments = arguments;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
}

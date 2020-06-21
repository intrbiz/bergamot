package com.intrbiz.bergamot.model.message.agent.stat.process;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.agent.model.process-info")
public class ProcessInfo extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("pid")
    private long pid;
    
    @JsonProperty("parent_pid")
    private long parentPid;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("executable")
    private String executable;
    
    @JsonProperty("current_working_directory")
    private String currentWorkingDirectory;
    
    @JsonProperty("command_line")
    private List<String> commandLine = new LinkedList<String>();
    
    @JsonProperty("user")
    private String user;
    
    @JsonProperty("group")
    private String group;
    
    @JsonProperty("threads")
    private long threads;
    
    @JsonProperty("started_at")
    private long startedAt;
    
    // memory
    
    @JsonProperty("size")
    private long size;
    
    @JsonProperty("resident")
    private long resident;
    
    @JsonProperty("share")
    private long share;
    
    // cpu
    
    @JsonProperty("total_time")
    private long totalTime;
    
    @JsonProperty("user_time")
    private long userTime;
    
    @JsonProperty("sys_time")
    private long sysTime;
    
    public ProcessInfo()
    {
        super();
    }

    public long getPid()
    {
        return pid;
    }

    public void setPid(long pid)
    {
        this.pid = pid;
    }

    public long getParentPid()
    {
        return parentPid;
    }

    public void setParentPid(long parentPid)
    {
        this.parentPid = parentPid;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getExecutable()
    {
        return executable;
    }

    public void setExecutable(String executable)
    {
        this.executable = executable;
    }

    public String getCurrentWorkingDirectory()
    {
        return currentWorkingDirectory;
    }

    public void setCurrentWorkingDirectory(String currentWorkingDirectory)
    {
        this.currentWorkingDirectory = currentWorkingDirectory;
    }

    public List<String> getCommandLine()
    {
        return commandLine;
    }

    public void setCommandLine(List<String> commandLine)
    {
        this.commandLine = commandLine;
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

    public long getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(long startedAt)
    {
        this.startedAt = startedAt;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public long getResident()
    {
        return resident;
    }

    public void setResident(long resident)
    {
        this.resident = resident;
    }

    public long getShare()
    {
        return share;
    }

    public void setShare(long share)
    {
        this.share = share;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    public void setTotalTime(long totalTime)
    {
        this.totalTime = totalTime;
    }

    public long getUserTime()
    {
        return userTime;
    }

    public void setUserTime(long userTime)
    {
        this.userTime = userTime;
    }

    public long getSysTime()
    {
        return sysTime;
    }

    public void setSysTime(long sysTime)
    {
        this.sysTime = sysTime;
    }

    public long getThreads()
    {
        return threads;
    }

    public void setThreads(long threads)
    {
        this.threads = threads;
    }
}

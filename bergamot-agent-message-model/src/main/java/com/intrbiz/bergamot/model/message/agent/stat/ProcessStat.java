package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.stat.process.ProcessInfo;

@JsonTypeName("bergamot.agent.stat.process")
public class ProcessStat extends AgentMessage
{
    @JsonProperty("total")
    private long total;
    
    @JsonProperty("threads")
    private long threads;
    
    @JsonProperty("running")
    private long running;
    
    @JsonProperty("sleeping")
    private long sleeping;
    
    @JsonProperty("idle")
    private long idle;
    
    @JsonProperty("stopped")
    private long stopped;
    
    @JsonProperty("zombie")
    private long zombie;
    
    @JsonProperty("processes")
    private List<ProcessInfo> processes = new LinkedList<ProcessInfo>();

    public ProcessStat()
    {
        super();
    }

    public ProcessStat(AgentMessage message)
    {
        super(message);
    }

    public ProcessStat(String id)
    {
        super(id);
    }

    public List<ProcessInfo> getProcesses()
    {
        return processes;
    }

    public void setProcesses(List<ProcessInfo> processes)
    {
        this.processes = processes;
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public long getThreads()
    {
        return threads;
    }

    public void setThreads(long threads)
    {
        this.threads = threads;
    }

    public long getRunning()
    {
        return running;
    }

    public void setRunning(long running)
    {
        this.running = running;
    }

    public long getSleeping()
    {
        return sleeping;
    }

    public void setSleeping(long sleeping)
    {
        this.sleeping = sleeping;
    }

    public long getIdle()
    {
        return idle;
    }

    public void setIdle(long idle)
    {
        this.idle = idle;
    }

    public long getStopped()
    {
        return stopped;
    }

    public void setStopped(long stopped)
    {
        this.stopped = stopped;
    }

    public long getZombie()
    {
        return zombie;
    }

    public void setZombie(long zombie)
    {
        this.zombie = zombie;
    }
}

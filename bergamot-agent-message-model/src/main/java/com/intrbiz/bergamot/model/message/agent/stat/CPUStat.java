package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUInfo;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUTime;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUUsage;

@JsonTypeName("bergamot.agent.stat.cpu")
public class CPUStat extends AgentMessage
{
    @JsonProperty("cpu-count")
    private int cpuCount = 0;

    @JsonProperty("load")
    private List<Double> load = new ArrayList<Double>(3);

    @JsonProperty("total-usage")
    private CPUUsage totalUsage;

    @JsonProperty("usage")
    private List<CPUUsage> usage = new ArrayList<CPUUsage>();

    @JsonProperty("info")
    private List<CPUInfo> info = new ArrayList<CPUInfo>();

    @JsonProperty("total-time")
    private CPUTime totalTime;

    @JsonProperty("time")
    private List<CPUTime> time = new ArrayList<CPUTime>();

    public CPUStat()
    {
        super();
    }

    public CPUStat(AgentMessage message)
    {
        super(message);
    }

    public CPUStat(String id)
    {
        super(id);
    }

    public int getCpuCount()
    {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount)
    {
        this.cpuCount = cpuCount;
    }

    public List<Double> getLoad()
    {
        return load;
    }

    public void setLoad(List<Double> load)
    {
        this.load = load;
    }

    public CPUUsage getTotalUsage()
    {
        return totalUsage;
    }

    public void setTotalUsage(CPUUsage totalUsage)
    {
        this.totalUsage = totalUsage;
    }

    public List<CPUUsage> getUsage()
    {
        return usage;
    }

    public void setUsage(List<CPUUsage> usage)
    {
        this.usage = usage;
    }

    public List<CPUInfo> getInfo()
    {
        return info;
    }

    public void setInfo(List<CPUInfo> info)
    {
        this.info = info;
    }

    public CPUTime getTotalTime()
    {
        return totalTime;
    }

    public void setTotalTime(CPUTime totalTime)
    {
        this.totalTime = totalTime;
    }

    public List<CPUTime> getTime()
    {
        return time;
    }

    public void setTime(List<CPUTime> time)
    {
        this.time = time;
    }
}

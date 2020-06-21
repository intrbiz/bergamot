package com.intrbiz.bergamot.model.message.agent.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.stat.mem")
public class MemStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("ram")
    private long ram;
    
    @JsonProperty("total-memory")
    private long totalMemory;
    
    @JsonProperty("used-memory")
    private long usedMemory;
    
    @JsonProperty("free-memory")
    private long freeMemory;
    
    @JsonProperty("actual-used-memory")
    private long actualUsedMemory;
    
    @JsonProperty("actual-free-memory")
    private long actualFreeMemory;
    
    @JsonProperty("used-memory-percentage")
    private double usedMemoryPercentage;
    
    @JsonProperty("free-memory-percentage")
    private double freeMemoryPercentage;

    public MemStat()
    {
        super();
    }

    public MemStat(Message message)
    {
        super(message);
    }

    public long getRam()
    {
        return ram;
    }

    public void setRam(long ram)
    {
        this.ram = ram;
    }

    public long getTotalMemory()
    {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory)
    {
        this.totalMemory = totalMemory;
    }

    public long getUsedMemory()
    {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory)
    {
        this.usedMemory = usedMemory;
    }

    public long getFreeMemory()
    {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory)
    {
        this.freeMemory = freeMemory;
    }

    public long getActualUsedMemory()
    {
        return actualUsedMemory;
    }

    public void setActualUsedMemory(long actualUsedMemory)
    {
        this.actualUsedMemory = actualUsedMemory;
    }

    public long getActualFreeMemory()
    {
        return actualFreeMemory;
    }

    public void setActualFreeMemory(long actualFreeMemory)
    {
        this.actualFreeMemory = actualFreeMemory;
    }

    public double getUsedMemoryPercentage()
    {
        return usedMemoryPercentage;
    }

    public void setUsedMemoryPercentage(double usedMemoryPercentage)
    {
        this.usedMemoryPercentage = usedMemoryPercentage;
    }

    public double getFreeMemoryPercentage()
    {
        return freeMemoryPercentage;
    }

    public void setFreeMemoryPercentage(double freeMemoryPercentage)
    {
        this.freeMemoryPercentage = freeMemoryPercentage;
    }
}

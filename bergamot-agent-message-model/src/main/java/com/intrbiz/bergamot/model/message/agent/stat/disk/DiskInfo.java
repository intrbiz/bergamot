package com.intrbiz.bergamot.model.message.agent.stat.disk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.agent.model.disk-info")
public class DiskInfo extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("mount")
    private String mount;

    @JsonProperty("device")
    private String device;

    @JsonProperty("type")
    private String type;

    @JsonProperty("size")
    private long size;

    @JsonProperty("available")
    private long available;

    @JsonProperty("used")
    private long used;

    @JsonProperty("used-percent")
    private double usedPercent;

    public DiskInfo()
    {
        super();
    }

    public String getMount()
    {
        return mount;
    }

    public void setMount(String mount)
    {
        this.mount = mount;
    }

    public String getDevice()
    {
        return device;
    }

    public void setDevice(String device)
    {
        this.device = device;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public long getAvailable()
    {
        return available;
    }

    public void setAvailable(long available)
    {
        this.available = available;
    }

    public long getUsed()
    {
        return used;
    }

    public void setUsed(long used)
    {
        this.used = used;
    }

    public double getUsedPercent()
    {
        return usedPercent;
    }

    public void setUsedPercent(double usedPercent)
    {
        this.usedPercent = usedPercent;
    }
}

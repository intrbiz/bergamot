package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;

@JsonTypeName("bergamot.agent.stat.disk")
public class DiskStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("disks")
    private List<DiskInfo> disks = new LinkedList<DiskInfo>();

    public DiskStat()
    {
        super();
    }

    public DiskStat(Message message)
    {
        super(message);
    }

    public List<DiskInfo> getDisks()
    {
        return disks;
    }

    public void setDisks(List<DiskInfo> disks)
    {
        this.disks = disks;
    }
}

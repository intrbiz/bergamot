package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIOInfo;

@JsonTypeName("bergamot.agent.stat.diskio")
public class DiskIOStat extends AgentMessage
{    
    @JsonProperty("disks")
    private List<DiskIOInfo> disks = new LinkedList<DiskIOInfo>();

    public DiskIOStat()
    {
        super();
    }

    public DiskIOStat(AgentMessage message)
    {
        super(message);
    }

    public DiskIOStat(String id)
    {
        super(id);
    }

    public List<DiskIOInfo> getDisks()
    {
        return disks;
    }

    public void setDisks(List<DiskIOInfo> disks)
    {
        this.disks = disks;
    }    
}

package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.agent.stat.netio.NetIOInfo;

@JsonTypeName("bergamot.agent.stat.netio")
public class NetIOStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("interfaces")
    private List<NetIOInfo> ifaces = new LinkedList<NetIOInfo>();

    public NetIOStat()
    {
        super();
    }

    public NetIOStat(Message message)
    {
        super(message);
    }

    public List<NetIOInfo> getIfaces()
    {
        return ifaces;
    }

    public void setIfaces(List<NetIOInfo> ifaces)
    {
        this.ifaces = ifaces;
    }
}

package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.netcon")
public class CheckNetCon extends AgentMessage
{
    private boolean client = false;
    
    private boolean server = true;
    
    private boolean tcp = true;
    
    private boolean udp = true;
    
    private boolean unix = false;
    
    private boolean raw = false;
    
    public CheckNetCon()
    {
        super();
    }

    public CheckNetCon(AgentMessage message)
    {
        super(message);
    }

    public CheckNetCon(String id)
    {
        super(id);
    }

    public boolean isClient()
    {
        return client;
    }

    public void setClient(boolean client)
    {
        this.client = client;
    }

    public boolean isServer()
    {
        return server;
    }

    public void setServer(boolean server)
    {
        this.server = server;
    }

    public boolean isTcp()
    {
        return tcp;
    }

    public void setTcp(boolean tcp)
    {
        this.tcp = tcp;
    }

    public boolean isUdp()
    {
        return udp;
    }

    public void setUdp(boolean udp)
    {
        this.udp = udp;
    }

    public boolean isUnix()
    {
        return unix;
    }

    public void setUnix(boolean unix)
    {
        this.unix = unix;
    }

    public boolean isRaw()
    {
        return raw;
    }

    public void setRaw(boolean raw)
    {
        this.raw = raw;
    }
}

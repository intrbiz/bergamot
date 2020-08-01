package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.netcon")
public class CheckNetCon extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("client")
    private boolean client = false;
    
    @JsonProperty("server")
    private boolean server = true;
    
    @JsonProperty("tcp")
    private boolean tcp = true;
    
    @JsonProperty("udp")
    private boolean udp = true;
    
    @JsonProperty("unix")
    private boolean unix = false;
    
    @JsonProperty("raw")
    private boolean raw = false;
    
    /**
     * Optionally filter on the local port;
     */
    @JsonProperty("local-port")
    private int localPort = 0;
    
    /**
     * Optionally filter on the remote port;
     */
    @JsonProperty("remote-port")
    private int remotePort = 0;
    
    /**
     * Optionally filter on the local address;
     */
    @JsonProperty("local-address")
    private String localAddress;
    
    /**
     * Optionally filter on the remote address;
     */
    @JsonProperty("remote-sddress")
    private String remoteAddress;
    
    public CheckNetCon()
    {
        super();
    }

    public CheckNetCon(boolean client, boolean server, boolean tcp, boolean udp, boolean unix, boolean raw, int localPort, int remotePort, String localAddress, String remoteAddress)
    {
        super();
        this.client = client;
        this.server = server;
        this.tcp = tcp;
        this.udp = udp;
        this.unix = unix;
        this.raw = raw;
        this.localPort = localPort;
        this.remotePort = remotePort;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    public CheckNetCon(Message message)
    {
        super(message);
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

    public int getLocalPort()
    {
        return localPort;
    }

    public void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public void setRemotePort(int remotePort)
    {
        this.remotePort = remotePort;
    }

    public String getLocalAddress()
    {
        return localAddress;
    }

    public void setLocalAddress(String localAddress)
    {
        this.localAddress = localAddress;
    }

    public String getRemoteAddress()
    {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress)
    {
        this.remoteAddress = remoteAddress;
    }
}

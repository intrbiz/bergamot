package com.intrbiz.bergamot.model.message.agent.stat.who;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.who-info")
public class WhoInfo extends AgentType
{
    @JsonProperty("user")
    private String user;
    
    @JsonProperty("device")
    private String device;
    
    @JsonProperty("host")
    private String host;
    
    @JsonProperty("time")
    private long time;
    
    public WhoInfo()
    {
        super();
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getDevice()
    {
        return device;
    }

    public void setDevice(String device)
    {
        this.device = device;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }
}

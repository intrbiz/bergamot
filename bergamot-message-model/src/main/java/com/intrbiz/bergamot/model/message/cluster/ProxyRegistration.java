package com.intrbiz.bergamot.model.message.cluster;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Registration information for a proxy.
 * 
 */
@JsonTypeName("bergamot.cluster.registry.proxy")
public class ProxyRegistration extends MessageObject implements Comparable<ProxyRegistration>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The id of the proxy
     */
    @JsonProperty("id")
    private UUID id;
    
    /**
     * When this proxy registered
     */
    @JsonProperty("registered")
    private long registered;
    
    /**
     * The proxy application string
     */
    @JsonProperty("application")
    private String application;
    
    /**
     * Provided info text for this proxy
     */
    @JsonProperty("info")
    private String info;
    
    /**
     * The hostname of the machine
     */
    @JsonProperty("host_name")
    private String hostName;
    
    public ProxyRegistration()
    {
        super();
    }
    
    public ProxyRegistration(UUID id, long registered, String application, String info, String hostName)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.registered = registered;
        this.application = application;
        this.info = info;
        this.hostName = hostName;
    }

    public UUID getId()
    {
        return id;
    }

    public long getRegistered()
    {
        return this.registered;
    }

    public void setRegistered(long registered)
    {
        this.registered = registered;
    }

    public String getApplication()
    {
        return application;
    }

    public String getInfo()
    {
        return info;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ProxyRegistration other = (ProxyRegistration) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int compareTo(ProxyRegistration o)
    {
        return this.id.compareTo(o.id);
    }
}

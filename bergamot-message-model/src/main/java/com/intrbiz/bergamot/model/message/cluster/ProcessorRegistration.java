package com.intrbiz.bergamot.model.message.cluster;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Registration data for a processing pool
 */
@JsonTypeName("bergamot.cluster.registry.processor")
public class ProcessorRegistration extends MessageObject implements Comparable<ProcessorRegistration>
{
    private static final long serialVersionUID = 1L;

    /**
     * The id of the notifier
     */
    @JsonProperty("id")
    private UUID id;
    
    /**
     * When this processor registered
     */
    @JsonProperty("registered")
    private long registered;
    
    /**
     * The worker application string
     */
    @JsonProperty("application")
    private String application;
    
    /**
     * Provided info text for this processor
     */
    @JsonProperty("info")
    private String info;
    
    /**
     * The host name for this processor
     */
    @JsonProperty("host_name")
    private String hostName;

    public ProcessorRegistration()
    {
        super();
    }

    public ProcessorRegistration(UUID id, long registered, String application, String info, String hostName)
    {
        super();
        this.id = id;
        this.application = application;
        this.info = info;
        this.hostName = hostName;
    }

    public UUID getId()
    {
        return this.id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getApplication()
    {
        return this.application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }

    public long getRegistered()
    {
        return this.registered;
    }

    public void setRegistered(long registered)
    {
        this.registered = registered;
    }

    public String getInfo()
    {
        return this.info;
    }

    public void setInfo(String info)
    {
        this.info = info;
    }

    public String getHostName()
    {
        return this.hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }
    
    @JsonIgnore
    public ProcessorRegistration id(UUID id)
    {
        this.id = id;
        return this;
    }
    
    @JsonIgnore
    public ProcessorRegistration registered(long registered)
    {
        this.registered = registered;
        return this;
    }
    
    @JsonIgnore
    public ProcessorRegistration info(String application, String hostName, String info)
    {
        this.application = application;
        this.hostName = hostName;
        this.info = info;
        return this;
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
        ProcessorRegistration other = (ProcessorRegistration) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int compareTo(ProcessorRegistration o)
    {
        return this.id.compareTo(o.id);
    }
}

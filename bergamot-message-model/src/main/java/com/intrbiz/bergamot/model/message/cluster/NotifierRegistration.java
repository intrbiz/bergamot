package com.intrbiz.bergamot.model.message.cluster;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Registration information for a notifier.
 * 
 */
@JsonTypeName("bergamot.cluster.registry.notifier")
public class NotifierRegistration extends MessageObject implements Comparable<NotifierRegistration>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The id of the notifier
     */
    @JsonProperty("id")
    private UUID id;
    
    /**
     * When this notifier registered
     */
    @JsonProperty("registered")
    private long registered;
    
    /**
     * The site ids that this worker is restricted to 
     */
    @JsonProperty("restricted_site_ids")
    private Set<UUID> restrictedSiteIds;
    
    /**
     * The engines available to send notifications
     */
    @JsonProperty("available_engines")
    private Set<String> availableEngines;
    
    /**
     * Is this a proxy worker
     */
    @JsonProperty("proxy")
    private boolean proxy;
    
    /**
     * The worker application string
     */
    @JsonProperty("application")
    private String application;
    
    /**
     * Provided info text for this worker
     */
    @JsonProperty("info")
    private String info;
    
    @JsonProperty("host_name")
    private String hostName;
    
    public NotifierRegistration()
    {
        super();
    }
    
    public NotifierRegistration(UUID id, long registered, boolean proxy, String application, String info, String hostName, Set<UUID> restrictedSiteIds, Set<String> availableEngines)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.registered = registered;
        this.proxy = proxy;
        this.application = application;
        this.info = info;
        this.hostName = hostName;
        this.restrictedSiteIds = (restrictedSiteIds == null || restrictedSiteIds.isEmpty()) ? new HashSet<>() : restrictedSiteIds;
        this.availableEngines = Objects.requireNonNull(availableEngines);
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

    public Set<UUID> getRestrictedSiteIds()
    {
        return restrictedSiteIds;
    }
    
    public boolean isSiteRestricted()
    {
        return this.restrictedSiteIds != null;
    }

    public Set<String> getAvailableEngines()
    {
        return availableEngines;
    }

    public boolean isProxy()
    {
        return proxy;
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
    
    @JsonIgnore
    public NotifierRegistration registered(long registered)
    {
        this.registered = registered;
        return this;
    }
    
    @JsonIgnore
    public NotifierRegistration id(UUID id)
    {
        this.id = id;
        return this;
    }

    public void setRestrictedSiteIds(Set<UUID> restrictedSiteIds)
    {
        this.restrictedSiteIds = restrictedSiteIds;
    }
    
    @JsonIgnore
    public NotifierRegistration restrictedSiteIds(UUID... restrictedSiteIds)
    {
        if (this.restrictedSiteIds == null) this.restrictedSiteIds = new HashSet<>();
        Collections.addAll(this.restrictedSiteIds, restrictedSiteIds);
        return this;
    }
    
    @JsonIgnore
    public NotifierRegistration restrictedSiteIds(Collection<UUID> restrictedSiteIds)
    {
        if (this.restrictedSiteIds == null) this.restrictedSiteIds = new HashSet<>();
        this.restrictedSiteIds.addAll(restrictedSiteIds);
        return this;
    }

    public void setAvailableEngines(Set<String> availableEngines)
    {
        this.availableEngines = availableEngines;
    }
    
    @JsonIgnore
    public NotifierRegistration availableEngines(String... availableEngines)
    {
        if (this.availableEngines == null) this.availableEngines = new HashSet<>();
        Collections.addAll(this.availableEngines, availableEngines);
        return this;
    }
    
    @JsonIgnore
    public NotifierRegistration availableEngines(Collection<String> availableEngines)
    {
        if (this.availableEngines == null) this.availableEngines = new HashSet<>();
        this.availableEngines.addAll(availableEngines);
        return this;
    }

    public void setProxy(boolean proxy)
    {
        this.proxy = proxy;
    }
    
    public NotifierRegistration proxy(boolean proxy)
    {
        this.proxy = proxy;
        return this;
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
    
    @JsonIgnore
    public NotifierRegistration info(String application, String hostName, String info)
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
        NotifierRegistration other = (NotifierRegistration) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int compareTo(NotifierRegistration o)
    {
        return this.id.compareTo(o.id);
    }
}

package com.intrbiz.bergamot.model.message.cluster;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Registration information for a worker.
 * 
 * Workers are available to execute checks and are distributed both 
 * geographically and logically.  Bergamot allows for check executions 
 * to routed across the available workers.
 * 
 */
@JsonTypeName("bergamot.cluster.registry.worker")
public class WorkerRegistration extends MessageObject implements Comparable<WorkerRegistration>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The id of the worker
     */
    @JsonProperty("id")
    private UUID id;
    
    /**
     * When this worker registered
     */
    @JsonProperty("registered")
    private long registered;
    
    /**
     * The site ids that this worker is restricted to 
     */
    @JsonProperty("restricted_site_ids")
    private Set<UUID> restrictedSiteIds;
    
    /**
     * The worker pool this worker has be placed into
     */
    @JsonProperty("worker_pool")
    private String workerPool;
    
    /**
     * The engines available to execute checks by this worker
     */
    @JsonProperty("available_engines")
    private Set<String> availableEngines;
    
    /**
     * Is this a proxy worker
     */
    @JsonProperty("proxy")
    private boolean proxy;
    
    @JsonProperty("proxy_id")
    private UUID proxyId;
    
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
    
    /**
     * The hostname of the machine
     */
    @JsonProperty("host_name")
    private String hostName;
    
    public WorkerRegistration()
    {
        super();
    }
    
    public WorkerRegistration(UUID id, long registered, boolean proxy, UUID proxyId, String application, String info, String hostName, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.registered = registered;
        this.proxy = proxy;
        this.proxyId = proxyId;
        this.application = application;
        this.info = info;
        this.hostName = hostName;
        this.restrictedSiteIds = (restrictedSiteIds == null || restrictedSiteIds.isEmpty()) ? new HashSet<>() : restrictedSiteIds;
        this.workerPool = workerPool;
        this.availableEngines = Objects.requireNonNull(availableEngines);
    }
    
    public WorkerRegistration(UUID id, long registered, String application, String info, String hostName, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines)
    {
        this(id, registered, false, null, application, info, hostName, restrictedSiteIds, workerPool, availableEngines);
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

    public String getWorkerPool()
    {
        return workerPool;
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

    public void setRestrictedSiteIds(Set<UUID> restrictedSiteIds)
    {
        this.restrictedSiteIds = restrictedSiteIds;
    }

    public void setWorkerPool(String workerPool)
    {
        this.workerPool = workerPool;
    }

    public void setAvailableEngines(Set<String> availableEngines)
    {
        this.availableEngines = availableEngines;
    }

    public void setProxy(boolean proxy)
    {
        this.proxy = proxy;
    }

    public UUID getProxyId()
    {
        return this.proxyId;
    }

    public void setProxyId(UUID proxyId)
    {
        this.proxyId = proxyId;
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
        WorkerRegistration other = (WorkerRegistration) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int compareTo(WorkerRegistration o)
    {
        return this.id.compareTo(o.id);
    }
}

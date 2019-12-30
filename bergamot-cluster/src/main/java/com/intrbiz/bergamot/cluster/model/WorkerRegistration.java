package com.intrbiz.bergamot.cluster.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Registration information for a worker.
 * 
 * Workers are available to execute checks and are distributed both 
 * geographically and logically.  Bergamot allows for check executions 
 * to routed across the available workers.
 * 
 */
public class WorkerRegistration implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The id of the worker
     */
    private UUID id;
    
    /**
     * The site ids that this worker is restricted to 
     */
    private UUID[] restrictedSiteIds;
    
    /**
     * The worker pool this worker has be placed into
     */
    private String workerPool;
    
    /**
     * The engines available to execute checks by this worker
     */
    private String[] availableEngines;
    
    /**
     * Is this a proxy worker
     */
    private boolean proxy;
    
    /**
     * The worker application string
     */
    private String application;
    
    /**
     * Provided info text for this worker
     */
    private String info;
    
    private String hostName;
    
    public WorkerRegistration(UUID id, boolean proxy, String application, String info, String hostName, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.proxy = proxy;
        this.application = application;
        this.info = info;
        this.hostName = hostName;
        this.restrictedSiteIds = (restrictedSiteIds == null || restrictedSiteIds.isEmpty()) ? null : restrictedSiteIds.toArray(new UUID[restrictedSiteIds.size()]);
        this.workerPool = workerPool;
        this.availableEngines = Objects.requireNonNull(availableEngines).toArray(new String[availableEngines.size()]);
    }

    public UUID getId()
    {
        return id;
    }

    public UUID[] getRestrictedSiteIds()
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

    public String[] getAvailableEngines()
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
    public String toString()
    {
        return "WorkerRegistration [id=" + id + ", restrictedSiteIds=" + Arrays.toString(restrictedSiteIds) + ", workerPool=" + workerPool + ", availableEngines=" + Arrays.toString(availableEngines) + ", proxy=" + proxy + ", application=" + application + ", info=" + info + ", hostName=" + hostName + "]";
    }
}

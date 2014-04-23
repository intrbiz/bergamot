package com.intrbiz.bergamot.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.config.model.ServiceCfg;
import com.intrbiz.bergamot.model.message.task.ExecuteCheck;

/**
 * Some software service running on a host which needs to be checked
 */
public class Service extends Check
{
    private Host host;

    private Set<ServiceGroup> serviceGroups = new HashSet<ServiceGroup>();

    public Service()
    {
        super();
    }
    
    public final String getType()
    {
        return "service";
    }

    public Host getHost()
    {
        return host;
    }

    public void setHost(Host host)
    {
        this.host = host;
    }

    public void configure(ServiceCfg config)
    {
        this.name = config.resolveServiceDescription();
        this.displayName = Util.coalesceEmpty(config.resolveDisplayName(), this.name);
        this.alertAttemptThreshold = config.resolveMaxCheckAttempts();
        this.recoveryAttemptThreshold = config.resolveMaxCheckAttempts();
        this.checkInterval = TimeUnit.MINUTES.toMillis(config.resolveCheckInterval());
        this.retryInterval = TimeUnit.MINUTES.toMillis(config.resolveRetryInterval());
    }

    public Set<ServiceGroup> getServicegroups()
    {
        return serviceGroups;
    }

    public void addServicegroup(ServiceGroup serviceGroup)
    {
        this.serviceGroups.add(serviceGroup);
    }

    public void setServicegroups(Set<ServiceGroup> serviceGroups)
    {
        this.serviceGroups = serviceGroups;
    }

    protected void setCheckParameters(ExecuteCheck executeCheck)
    {
        super.setCheckParameters(executeCheck);
        // intrinsic parameters
        executeCheck.addParameter("HOSTADDRESS", this.getHost().getAddress());
        executeCheck.addParameter("HOSTNAME", this.getHost().getName());
        executeCheck.addParameter("SERVICEDESCRIPTION", this.getName());
    }

    public String toString()
    {
        return "Service (" + this.id + ") " + this.name + " on host " + this.getHost().getName() + " check " + this.checkCommand;
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
        Service other = (Service) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }
}

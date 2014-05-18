package com.intrbiz.bergamot.model;

import java.util.concurrent.TimeUnit;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.configuration.Configurable;

/**
 * Some software service running on a host which needs to be checked
 */
public class Service extends ActiveCheck implements Configurable<ServiceCfg>
{
    private Host host;

    private ServiceCfg config;

    public Service()
    {
        super();
    }

    @Override
    public void configure(ServiceCfg cfg)
    {
        this.config = cfg;
        ServiceCfg rcfg = cfg.resolve();
        //
        this.name = rcfg.getName();
        this.displayName = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.alertAttemptThreshold = rcfg.getState().getFailedAfter();
        this.recoveryAttemptThreshold = rcfg.getState().getRecoversAfter();
        this.checkInterval = TimeUnit.MINUTES.toMillis(rcfg.getSchedule().getEvery());
        this.retryInterval = TimeUnit.MINUTES.toMillis(rcfg.getSchedule().getRetryEvery());
        this.enabled = rcfg.getEnabledBooleanValue();
        this.suppressed = rcfg.getSuppressedBooleanValue();
        // initial state
        this.getState().setAttempt(this.recoveryAttemptThreshold);
        if (rcfg.getInitialState() != null)
        {
            this.getState().setStatus(Status.valueOf(rcfg.getInitialState().getStatus().toUpperCase()));
            this.getState().setOk(this.getState().getStatus().isOk());
            this.getState().setOutput(Util.coalesce(rcfg.getInitialState().getOutput(), ""));
            this.getState().setLastHardStatus(this.getState().getStatus());
            this.getState().setLastHardOk(this.getState().isOk());
            this.getState().setLastHardOutput(this.getState().getOutput());
        }
    }

    @Override
    public ServiceCfg getConfiguration()
    {
        return this.config;
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

    @Override
    public ServiceMO toMO()
    {
        ServiceMO mo = new ServiceMO();
        super.toMO(mo);
        mo.setHost(this.getHost().toMO());
        return mo;
    }
}

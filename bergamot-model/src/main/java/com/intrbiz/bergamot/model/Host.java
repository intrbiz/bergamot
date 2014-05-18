package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.state.GroupState;
import com.intrbiz.configuration.Configurable;

/**
 * A host - some form of network connected device that is to be checked
 */
public class Host extends ActiveCheck implements Configurable<HostCfg>
{
    private String address;

    private Map<String, Service> services = new TreeMap<String, Service>();
    
    private Map<String, Trap> traps = new TreeMap<String, Trap>();

    /**
     * The location of this host
     */
    @JsonProperty("location")
    private Location location;

    private HostCfg config;

    public Host()
    {
        super();
    }

    @Override
    public void configure(HostCfg cfg)
    {
        this.config = cfg;
        HostCfg rcfg = cfg.resolve();
        //
        this.name = rcfg.getName();
        this.address = Util.coalesceEmpty(rcfg.getAddress(), this.name);
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
    public HostCfg getConfiguration()
    {
        return this.config;
    }

    public final String getType()
    {
        return "host";
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }
    
    // services

    public Set<String> getServiceNames()
    {
        return this.services.keySet();
    }

    public Collection<Service> getServices()
    {
        return this.services.values();
    }

    public void addService(Service service)
    {
        this.services.put(service.getName(), service);
        service.setHost(this);
    }

    public Service getService(String name)
    {
        return this.services.get(name);
    }

    public boolean containsService(String name)
    {
        return this.services.containsKey(name);
    }

    public int getServiceCount()
    {
        return this.services.size();
    }

    public GroupState getServicesState()
    {
        return GroupState.compute(this.getServices(), null, null);
    }
    
    // traps
    
    public Set<String> getTrapNames()
    {
        return this.traps.keySet();
    }

    public Collection<Trap> getTraps()
    {
        return this.traps.values();
    }

    public void addTrap(Trap trap)
    {
        this.traps.put(trap.getName(), trap);
        trap.setHost(this);
    }

    public Trap getTrap(String name)
    {
        return this.traps.get(name);
    }

    public boolean containsTrap(String name)
    {
        return this.traps.containsKey(name);
    }

    public int getTrapCount()
    {
        return this.traps.size();
    }

    public GroupState getTrapsState()
    {
        return GroupState.compute(this.getTraps(), null, null);
    }
    
    // location

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public String toString()
    {
        return "Host (" + this.id + ") " + this.name + " check " + this.checkCommand;
    }

    @Override
    public HostMO toMO()
    {
        HostMO mo = new HostMO();
        super.toMO(mo);
        mo.setAddress(this.getAddress());
        return mo;
    }
}

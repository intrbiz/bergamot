package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A host - some form of network connected device that is to be checked
 */
@SQLTable(schema = BergamotDB.class, name = "host", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class Host extends ActiveCheck<HostMO, HostCfg>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The IP address of this host, this might be an IP address or a DNS name
     */
    @SQLColumn(index = 1, name = "address", since = @SQLVersion({ 1, 0, 0 }))
    private String address;

    /**
     * The ID of the location which this host is physically located in
     */
    @SQLColumn(index = 2, name = "location_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID locationId;
    
    /**
     * The UUID of the agent that might be used for this host
     */
    @SQLColumn(index = 3, name = "agent_id", since = @SQLVersion({ 1, 9, 0 }))
    private UUID agentId;

    public Host()
    {
        super();
    }

    @Override
    public void configure(HostCfg cfg, HostCfg rcfg)
    {
        super.configure(cfg, rcfg);
        this.address = Util.coalesceEmpty(rcfg.getAddress(), this.name);
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

    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    // services

    public List<Service> getServices()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getServicesOnHost(this.getId());
        }
    }
    
    public Service getService(String name)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getServiceOnHost(this.getId(), name);
        }
    }

    public void addService(Service service)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addServiceToHost(this, service);
        }
    }
    
    public void removeService(Service service)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeServiceFromHost(this, service);
        }
    }

    // traps

    public Collection<Trap> getTraps()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getTrapsOnHost(this.getId());
        }
    }

    public void addTrap(Trap trap)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addTrapToHost(this, trap);
        }
    }
    
    public void removeTrap(Trap trap)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeTrapFromHost(this, trap);
        }
    }

    public Trap getTrap(String name)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getTrapOnHost(this.getId(), name);
        }
    }

    // location

    public Location getLocation()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getLocation(this.locationId);
        }
    }

    public UUID getLocationId()
    {
        return this.locationId;
    }

    public void setLocationId(UUID locationId)
    {
        this.locationId = locationId;
    }

    public String toString()
    {
        return "Host (" + this.id + ") " + this.name + " check " + this.getCheckCommand();
    }

    @Override
    public HostMO toMO(boolean stub)
    {
        HostMO mo = new HostMO();
        super.toMO(mo, stub);
        mo.setAddress(this.getAddress());
        if (!stub)
        {
            mo.setServices(this.getServices().stream().map(Service::toStubMO).collect(Collectors.toList()));
            mo.setTraps(this.getTraps().stream().map(Trap::toStubMO).collect(Collectors.toList()));
            mo.setLocation(Util.nullable(this.getLocation(), Location::toStubMO));
        }
        return mo;
    }
    
    @Override
    public String resolveWorkerPool()
    {
        String workerPool = this.getWorkerPool();
        if (workerPool == null)
        {
            workerPool = Util.nullable(this.getLocation(), Location::resolveWorkerPool);
        }
        return workerPool;
    }
}

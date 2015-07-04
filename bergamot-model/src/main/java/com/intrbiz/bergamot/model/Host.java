package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    public void configure(HostCfg configuration, HostCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        this.address = Util.coalesceEmpty(resolvedConfiguration.getAddress(), this.name);
        this.agentId = resolvedConfiguration.getAgentId();
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
    
    @Override
    public UUID resolveAgentId()
    {
        return this.getAgentId();
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
    
    public Service getServiceByExternalRef(String externalref)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getServiceOnHostByExternalRef(this.getId(), externalRef);
        }
    }
    
    public Collection<Category<Service>> getCategorisedServices()
    {
        Map<String, Category<Service>> categories = new TreeMap<String, Category<Service>>();
        for (Service service : this.getServices())
        {
            // get the category for this service
            String categoryTag = Util.coalesceEmpty(service.resolveCategory(), "default");
            Category<Service> category = categories.get(categoryTag.toLowerCase());
            if (category == null)
            {
                category = new Category<Service>(categoryTag);
                categories.put(categoryTag.toLowerCase(), category);
            }
            // by application too?
            String applicationTag = service.resolveApplication();
            if (applicationTag == null) category.addCheck(service);
            else category.getOrAddApplication(applicationTag).addCheck(service);
        }
        return categories.values();
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
    
    public Trap getTrapByExternalRef(String externalRef)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getTrapOnHostByExternalRef(this.getId(), externalRef);
        }
    }
    
    public Collection<Category<Trap>> getCategorisedTraps()
    {
        Map<String, Category<Trap>> categories = new TreeMap<String, Category<Trap>>();
        for (Trap trap : this.getTraps())
        {
            // get the category for this service
            String categoryTag = Util.coalesceEmpty(trap.resolveCategory(), "default");
            Category<Trap> category = categories.get(categoryTag.toLowerCase());
            if (category == null)
            {
                category = new Category<Trap>(categoryTag);
                categories.put(categoryTag.toLowerCase(), category);
            }
            // by application too?
            String applicationTag = trap.resolveApplication();
            if (applicationTag == null) category.addCheck(trap);
            else category.getOrAddApplication(applicationTag).addCheck(trap);
        }
        return categories.values();
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

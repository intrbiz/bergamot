package com.intrbiz.bergamot.model;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * Some software service running on a host which needs to be checked
 */
@SQLTable(schema = BergamotDB.class, name = "service", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "host_name_unq", columns = {"host_id", "name"})
public class Service extends ActiveCheck<ServiceMO, ServiceCfg>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "host_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID hostId;
    
    @SQLColumn(index = 2, name = "category", since = @SQLVersion({ 2, 5, 0 }))
    private String category;

    @SQLColumn(index = 3, name = "application", since = @SQLVersion({ 2, 5, 0 }))
    private String application;

    public Service()
    {
        super();
    }

    @Override
    public void configure(ServiceCfg configuration, ServiceCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        this.category = resolvedConfiguration.getCategory();
        this.application = resolvedConfiguration.getApplication();
    }

    public final String getType()
    {
        return "service";
    }

    public UUID getHostId()
    {
        return this.hostId;
    }

    public void setHostId(UUID hostId)
    {
        this.hostId = hostId;
    }
    
    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }
    
    /**
     * Resolve the category tag for this Service
     * @return the category tag or null is not specified
     */
    public String resolveCategory()
    {
        if (! Util.isEmpty(this.getCategory())) return this.getCategory();
        CheckCommand checkCommand = this.getCheckCommand();
        if (checkCommand != null)
        {
            Command command = checkCommand.getCommand();
            if (command != null && (! Util.isEmpty(command.getCategory()))) return command.getCategory();
        }
        return null;
    }

    /**
     * Resolve the application tag for this Service
     * @return the application tag or null is not specified
     */
    public String resolveApplication()
    {
        if (! Util.isEmpty(this.getApplication())) return this.getApplication();
        CheckCommand checkCommand = this.getCheckCommand();
        if (checkCommand != null)
        {
            Command command = checkCommand.getCommand();
            if (command != null && (! Util.isEmpty(command.getApplication()))) return command.getApplication();
        }
        return null;
    }
    
    @Override
    public UUID resolveAgentId()
    {
        Host host = this.getHost();
        return host == null ? null : host.getAgentId();
    }
    
    public Host getHost()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getHost(this.getHostId());
        }
    }
    
    @Override
    public boolean hasDependencies()
    {
        return true;
    }
    
    @Override
    public List<Check<?, ?>> getDepends()
    {
        List<Check<?, ?>> depends = super.getDepends();
        depends.add(0, this.getHost());
        return depends;
    }

    public String toString()
    {
        return "Service (" + this.id + ") " + this.name + " on host " + this.getHost().getName() + " check " + this.getCheckCommand();
    }

    @Override
    public ServiceMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        ServiceMO mo = new ServiceMO();
        super.toMO(mo, contact, options);
        mo.setCategory(this.resolveCategory());
        mo.setApplication(this.resolveApplication());
        if (options.contains(MOFlag.HOST))
        {
            Host host = this.getHost();
            if (host != null && (contact == null || contact.hasPermission("read", host))) mo.setHost(host.toStubMO(contact));
        }
        return mo;
    }
    
    @Override
    public String resolveWorkerPool()
    {
        String workerPool = this.getWorkerPool();
        if (workerPool == null)
        {
            workerPool = Util.nullable(this.getHost(), Host::resolveWorkerPool);
        }
        return workerPool;
    }
}

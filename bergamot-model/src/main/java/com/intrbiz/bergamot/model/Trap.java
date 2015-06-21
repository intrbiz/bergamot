package com.intrbiz.bergamot.model;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * The real world manifestation of a passive check
 */
@SQLTable(schema = BergamotDB.class, name = "trap", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "host_name_unq", columns = {"host_id", "name"})
public class Trap extends PassiveCheck<TrapMO, TrapCfg>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "host_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID hostId;
    
    @SQLColumn(index = 2, name = "category", since = @SQLVersion({ 2, 5, 0 }))
    private String category;

    @SQLColumn(index = 3, name = "application", since = @SQLVersion({ 2, 5, 0 }))
    private String application;

    public Trap()
    {
        super();
    }
    
    @Override
    public void configure(TrapCfg configuration, TrapCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        this.category = resolvedConfiguration.getCategory();
        this.application = resolvedConfiguration.getApplication();
    }

    @Override
    public final String getType()
    {
        return "trap";
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
     * Resolve the category tag for this Trap
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
     * Resolve the application tag for this Trap
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
    
    public Host getHost()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getHost(this.getHostId());
        }
    }

    @Override
    public TrapMO toMO(boolean stub)
    {
        TrapMO mo = new TrapMO();
        super.toMO(mo, stub);
        mo.setCategory(this.resolveCategory());
        mo.setApplication(this.resolveApplication());
        if (! stub)
        {
            mo.setHost(this.getHost().toStubMO());
        }
        return mo;
    }
}

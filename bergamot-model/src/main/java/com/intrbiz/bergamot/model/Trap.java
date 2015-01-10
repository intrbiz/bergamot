package com.intrbiz.bergamot.model;

import java.util.UUID;

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

    public Trap()
    {
        super();
    }
    
    @Override
    public void configure(TrapCfg configuration, TrapCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
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
        if (! stub)
        {
            mo.setHost(this.getHost().toStubMO());
        }
        return mo;
    }
}

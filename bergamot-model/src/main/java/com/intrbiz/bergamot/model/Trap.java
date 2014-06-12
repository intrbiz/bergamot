package com.intrbiz.bergamot.model;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.TrapCfgAdapter;
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
    
    @SQLColumn(index = 1, name = "configuration", type = "TEXT", adapter = TrapCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected TrapCfg configuration;
    
    @SQLColumn(index = 2, name = "host_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID hostId;

    public Trap()
    {
        super();
    }
    
    @Override
    public TrapCfg getConfiguration()
    {
        return configuration;
    }

    @Override
    public void setConfiguration(TrapCfg configuration)
    {
        this.configuration = configuration;
    }
    
    @Override
    public void configure(TrapCfg cfg)
    {
        super.configure(cfg);
        TrapCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
        this.alertAttemptThreshold = rcfg.getState().getFailedAfter();
        this.recoveryAttemptThreshold = rcfg.getState().getRecoversAfter();
        this.enabled = rcfg.getEnabledBooleanValue();
        this.suppressed = rcfg.getSuppressedBooleanValue();
        // initial state
        // TODO
        /*
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
        */
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

package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.ServiceCfgAdapter;
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
    @SQLColumn(index = 1, name = "configuration", type = "TEXT", adapter = ServiceCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected ServiceCfg configuration;
    
    @SQLColumn(index = 2, name = "host_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID hostId;

    public Service()
    {
        super();
    }
    
    @Override
    public ServiceCfg getConfiguration()
    {
        return configuration;
    }

    @Override
    public void setConfiguration(ServiceCfg configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void configure(ServiceCfg cfg)
    {
        super.configure(cfg);
        ServiceCfg rcfg = cfg.resolve();
        //
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
        this.alertAttemptThreshold = rcfg.getState().getFailedAfter();
        this.recoveryAttemptThreshold = rcfg.getState().getRecoversAfter();
        this.checkInterval = TimeUnit.MINUTES.toMillis(rcfg.getSchedule().getEvery());
        this.retryInterval = TimeUnit.MINUTES.toMillis(rcfg.getSchedule().getRetryEvery());
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
    
    public Host getHost()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getHost(this.getHostId());
        }
    }

    public String toString()
    {
        return "Service (" + this.id + ") " + this.name + " on host " + this.getHost().getName() + " check " + this.getCheckCommand();
    }

    @Override
    public ServiceMO toMO(boolean stub)
    {
        ServiceMO mo = new ServiceMO();
        super.toMO(mo, stub);
        if (! stub)
        {
            mo.setHost(this.getHost().toStubMO());
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

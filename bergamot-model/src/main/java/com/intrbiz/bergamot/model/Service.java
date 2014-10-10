package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public Service()
    {
        super();
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
        if (rcfg.getState() != null)
        {
            this.alertAttemptThreshold = rcfg.getState().getFailedAfter();
            this.recoveryAttemptThreshold = rcfg.getState().getRecoversAfter();
        }
        if (rcfg.getSchedule() != null)
        {
            this.checkInterval = TimeUnit.MINUTES.toMillis(rcfg.getSchedule().getEvery());
            this.retryInterval = TimeUnit.MINUTES.toMillis(rcfg.getSchedule().getRetryEvery());
        }
        this.enabled = rcfg.getEnabledBooleanValue();
        this.suppressed = rcfg.getSuppressedBooleanValue();
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

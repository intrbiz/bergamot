package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolProducer;
import com.intrbiz.bergamot.cluster.queue.WorkerProducer;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;


public abstract class AbstractScheduler implements Scheduler
{   
    private static final Logger logger = Logger.getLogger(AbstractScheduler.class);
    
    protected final UUID poolId;
    
    protected final WorkerProducer WorkerProducer;
    
    protected final ProcessingPoolProducer processingPoolProducer;
    
    public AbstractScheduler(UUID poolId, WorkerProducer WorkerProducer, ProcessingPoolProducer processingPoolProducer)
    {
        super();
        this.poolId = poolId;
        this.WorkerProducer = WorkerProducer;
        this.processingPoolProducer = processingPoolProducer;
    }
    
    public UUID getPoolId()
    {
        return this.poolId;
    }
    
    protected PublishStatus publishExecuteCheck(ExecuteCheck check)
    {
        if (logger.isTraceEnabled())
            logger.trace("Publishing execute check\n" + check);
        // publish
        PublishStatus status =  this.WorkerProducer.executeCheck(check);
        if (status != PublishStatus.Success)
        {
            this.publishFailedCheck(check, status);
        }
        return status;
    }
    
    protected void publishFailedCheck(ExecuteCheck check, PublishStatus status)
    {
        // we failed to execute the given check in time, oops!
        logger.warn("Failed to execute check (" + status + "): " + check.getId() + "\r\n" + check);
        // fake a timeout / error result and submit it
        ActiveResultMO result = new ActiveResultMO().fromCheck(check);
        if (status == PublishStatus.Unroutable)
        {
            result.error("No workers available which support this check");
        }
        else
        {
            result.timeout("Unable to publish check to worker");
        }
        this.processingPoolProducer.publishResult(this.poolId, result);
    }
    
    public void schedulePool(UUID siteId, int processingPool)
    {
        logger.info("Scheduling all checks in pool " + siteId + "." + processingPool);
        try (BergamotDB db = BergamotDB.connect())
        {
            for (Host host : db.listHostsInPool(siteId, processingPool))
            {
                this.schedule(host);
            }
            for (Service service : db.listServicesInPool(siteId, processingPool))
            {
                this.schedule(service);
            }
        }
    }

    @Override
    public void schedule(UUID checkId)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            ActiveCheck<?,?> check = db.getActiveCheck(checkId);
            if (check != null)
            {
                this.schedule(check);
            }
        }
    }
}

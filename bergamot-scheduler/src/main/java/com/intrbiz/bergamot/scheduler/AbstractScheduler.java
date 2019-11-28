package com.intrbiz.bergamot.scheduler;

import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.scheduler.CheckProducer.PublishStatus;


public abstract class AbstractScheduler implements Scheduler
{   
    private static final Logger logger = Logger.getLogger(AbstractScheduler.class);
    
    protected final UUID poolId;
    
    protected CheckProducer checkProducer;
    
    public AbstractScheduler(UUID poolId, CheckProducer checkProducer)
    {
        super();
        this.poolId = poolId;
        this.checkProducer = checkProducer;
    }
    
    public UUID getPoolId()
    {
        return this.poolId;
    }
    
    public void start() throws Exception
    {
    }
    
    protected PublishStatus publishExecuteCheck(ExecuteCheck check)
    {
        if (logger.isTraceEnabled())
            logger.trace("Publishing execute check\n" + check);
        // publish
        PublishStatus status =  this.checkProducer.publishCheck(check);
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
        this.checkProducer.publishFailedCheck(result);
    }
    
    @Override
    public void schedule(Collection<ActiveCheck<?, ?>> checks)
    {
        for (ActiveCheck<?, ?> check : checks)
        {
            this.schedule(check);
        }
    }
    
    public void schedulePool(UUID siteId, int processingPool)
    {
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
}

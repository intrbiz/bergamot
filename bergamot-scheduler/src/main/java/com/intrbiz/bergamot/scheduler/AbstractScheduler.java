package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.dispatcher.CheckDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerMessage;


public abstract class AbstractScheduler implements Scheduler
{
    private static final Logger logger = Logger.getLogger(AbstractScheduler.class);
    
    protected final UUID processorId;
    
    protected final CheckDispatcher checkDispatcher;
    
    protected final ProcessorDispatcher processorDispatcher;
    
    public AbstractScheduler(UUID processorId, CheckDispatcher checkDispatcher, ProcessorDispatcher processorDispatcher)
    {
        super();
        this.processorId = processorId;
        this.checkDispatcher = checkDispatcher;
        this.processorDispatcher = processorDispatcher;
    }
    
    protected PublishStatus publishExecuteCheck(ExecuteCheck check)
    {
        if (logger.isTraceEnabled())
            logger.trace("Publishing execute check\n" + check);
        // publish
        PublishStatus status =  this.checkDispatcher.dispatchCheck(check);
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
        ActiveResult result = new ActiveResult().fromCheck(check);
        if (status == PublishStatus.Unroutable)
        {
            result.error("No workers available which support this check");
        }
        else
        {
            result.timeout("Unable to publish check to worker");
        }
        this.processorDispatcher.dispatchResult(this.processorId, result);
    }
    
    public void schedulePool(int pool)
    {
        logger.info("Scheduling all checks in pool: " + pool);
        try (BergamotDB db = BergamotDB.connect())
        {
            for (Host host : db.listHostsInPool(pool))
            {
                logger.info("Scheduling host " + host.getId());
                this.schedule(host);
            }
            for (Service service : db.listServicesInPool(pool))
            {
                logger.info("Scheduling service " + service.getId());
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
    
    public void process(SchedulerMessage message)
    {
        if (message instanceof ScheduleCheck)
        {
            ScheduleCheck check = (ScheduleCheck) message;
            switch (check.getCommand())
            {
                case DISABLE:
                    this.disable(check.getCheckId());
                    break;
                case ENABLE:
                    this.enable(check.getCheckId());
                    break;
                case RESCHEDULE:
                    this.reschedule(check.getId(), check.getInterval());
                    break;
                case SCHEDULE:
                    this.schedule(check.getId());
                    break;
                case UNSCHEDULE:
                    this.unschedule(check.getId());
                    break;
            }
        }
    }
}

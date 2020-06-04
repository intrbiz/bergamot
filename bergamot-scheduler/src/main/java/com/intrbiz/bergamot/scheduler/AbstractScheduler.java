package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.ExecuteCheckAccountingEvent;
import com.intrbiz.bergamot.cluster.dispatcher.WorkerDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;


public abstract class AbstractScheduler implements Scheduler
{
    private static final Logger logger = Logger.getLogger(AbstractScheduler.class);
    
    protected final UUID processorId;
    
    protected final WorkerDispatcher checkDispatcher;
    
    protected final ProcessorDispatcher processorDispatcher;
    
    // accounting
    protected final Accounting accounting = Accounting.create(this.getClass());
    
    public AbstractScheduler(UUID processorId, WorkerDispatcher checkDispatcher, ProcessorDispatcher processorDispatcher)
    {
        super();
        this.processorId = processorId;
        this.checkDispatcher = checkDispatcher;
        this.processorDispatcher = processorDispatcher;
    }
    
    public PublishStatus executeCheck(ActiveCheck<?,?> check)
    {
        ExecuteCheck executeCheck = check.executeCheck();
        if (executeCheck != null)
        {
            // publish the check
            executeCheck.setProcessorId(this.processorId);
            PublishStatus result = this.publishExecuteCheck(executeCheck);
            if (result == PublishStatus.Success)
            {
                this.accounting.account(new ExecuteCheckAccountingEvent(executeCheck.getSiteId(), executeCheck.getId(), check.getId(), executeCheck.getEngine(), executeCheck.getExecutor(), executeCheck.getName()));
            }
            return result;
        }
        return PublishStatus.Failed;
    }
    
    protected PublishStatus publishExecuteCheck(ExecuteCheck check)
    {
        if (logger.isTraceEnabled())
            logger.trace("Publishing execute check\n" + check);
        // Publish
        PublishStatus status =  this.checkDispatcher.dispatchCheck(check);
        if (status != PublishStatus.Success)
        {
            // Publish a failure result
            this.publishFailedCheck(check, status);
        }
        return status;
    }
    
    protected void publishFailedCheck(ExecuteCheck check, PublishStatus status)
    {
        logger.warn("Failed to execute check (" + status + "): " + check.getId() + "\r\n" + check);
        ActiveResult result = new ActiveResult()._fromCheck(check);
        switch (status)
        {
            case AgentUnroutable:
                result.error("The agent is not available");
                break;
            case NoAgentId:
                result.action("The agent id is required");
                break;
            case Unroutable:
                result.error("No workers available which support this check");
                break;
            case Failed:
                result.timeout("Unable to publish check to worker");
                break;
            default:
                result.error("Unexpected publish status: " + status);
                break;
        }
        // Publish the result
        result.setProcessorId(this.processorId);
        PublishStatus resultStatus = this.processorDispatcher.dispatchResult(result);
        if (resultStatus != PublishStatus.Success)
        {
            logger.error("Failed to publish failure result, got: " + resultStatus + "!\n" + result);
        }
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
            else
            {
                logger.warn("Could not get check for id: " + checkId);
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
                    this.reschedule(check.getCheckId(), check.getInterval());
                    break;
                case SCHEDULE:
                    this.schedule(check.getCheckId());
                    break;
                case UNSCHEDULE:
                    this.unschedule(check.getCheckId());
                    break;
            }
        }
    }
}

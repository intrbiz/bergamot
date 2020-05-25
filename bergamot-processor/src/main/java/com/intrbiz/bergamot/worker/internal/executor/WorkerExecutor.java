package com.intrbiz.bergamot.worker.internal.executor;

import java.util.Set;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.internal.InternalExecutor;
import com.intrbiz.bergamot.worker.internal.InternalExecutorContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;

public class WorkerExecutor implements InternalExecutor
{    
    @Override
    public String getName()
    {
        return "workers";
    }

    @Override
    public void execute(ExecuteCheck check, InternalExecutorContext context) throws Exception
    {
        UUID siteId = check.getSiteId();
        String workerPool = Util.coalesceEmpty(check.getWorkerPool(), "any");
        Set<String> engines = check.getParameterCSV("engines");
        int warning = check.getIntParameter("warning", 2);
        int critical = check.getIntParameter("critical", 1);
        // filter the workers
        int workers = 0;
        for (WorkerRegistration worker : context.getLocator().getWorkerRegistry().getWorkers())
        {
            if ((worker.getRestrictedSiteIds() == null || worker.getRestrictedSiteIds().isEmpty() || worker.getRestrictedSiteIds().contains(siteId)) && 
                workerPool.equals(Util.coalesce(worker.getWorkerPool(), "any")) && 
                (engines.isEmpty() || worker.getAvailableEngines().containsAll(engines)))
            {
                workers++;
            }
        }
        context.publishResult(new ActiveResult().applyLessThanThreshold(workers, warning, critical, "Workers: " + workers));
        context.publishReading(new IntegerGaugeReading("workers", "#", workers, warning, critical, null, null));
    }
}

package com.intrbiz.bergamot.worker.internal.executor;

import java.util.Set;
import java.util.UUID;

import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.internal.InternalExecutor;
import com.intrbiz.bergamot.worker.internal.InternalExecutorContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;

public class NotifierExecutor implements InternalExecutor
{    
    @Override
    public String getName()
    {
        return "notifiers";
    }

    @Override
    public void execute(ExecuteCheck check, InternalExecutorContext context) throws Exception
    {
        UUID siteId = check.getSiteId();
        Set<String> engines = check.getParameterCSV("engines");
        int warning = check.getIntParameter("warning", 2);
        int critical = check.getIntParameter("critical", 1);
        // filter the workers
        int notifiers = 0;
        for (NotifierRegistration notifier : context.getLocator().getNotifierRegistry().getNotifiers())
        {
            if ((notifier.getRestrictedSiteIds() == null || notifier.getRestrictedSiteIds().isEmpty() || notifier.getRestrictedSiteIds().contains(siteId)) && 
                (engines.isEmpty() || notifier.getAvailableEngines().containsAll(engines)))
            {
                notifiers++;
            }
        }
        context.publishResult(new ActiveResult().applyLessThanThreshold(notifiers, warning, critical, "Notifiers: " + notifiers));
        context.publishReading(new IntegerGaugeReading("notifiers", "#", notifiers, warning, critical, null, null));
    }
}

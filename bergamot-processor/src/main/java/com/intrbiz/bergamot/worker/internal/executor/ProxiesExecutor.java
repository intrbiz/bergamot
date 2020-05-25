package com.intrbiz.bergamot.worker.internal.executor;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.internal.InternalExecutor;
import com.intrbiz.bergamot.worker.internal.InternalExecutorContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;

public class ProxiesExecutor implements InternalExecutor
{    
    @Override
    public String getName()
    {
        return "proxies";
    }

    @Override
    public void execute(ExecuteCheck check, InternalExecutorContext context) throws Exception
    {
        int warning = check.getIntParameter("warning", 2);
        int critical = check.getIntParameter("critical", 1);
        // filter the workers
        int proxies = context.getLocator().getProxyRegistry().count();
        context.publishResult(new ActiveResult().applyLessThanThreshold(proxies, warning, critical, "Proxies: " + proxies));
        context.publishReading(new IntegerGaugeReading("proxies", "#", proxies, warning, critical, null, null));
    }
}

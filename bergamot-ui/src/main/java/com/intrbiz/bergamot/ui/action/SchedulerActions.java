package com.intrbiz.bergamot.ui.action;

import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.scheduler.DisableCheck;
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.bergamot.queue.key.SchedulerKey;
import com.intrbiz.metadata.Action;
import com.intrbiz.queue.RoutedProducer;

public class SchedulerActions
{
    private SchedulerQueue queue;

    private RoutedProducer<SchedulerAction> schedulerActionProducer;

    public SchedulerActions()
    {
        this.queue = SchedulerQueue.open();
        this.schedulerActionProducer = this.queue.publishSchedulerActions();
    }

    @Action("enable-check")
    public void enableCheck(ActiveCheck<?, ?> check)
    {
        synchronized (this)
        {
            this.schedulerActionProducer.publish(new SchedulerKey(check.getSiteId(), check.getPool()), new EnableCheck(check.toStubMO()));
        }
    }
    
    @Action("disable-check")
    public void disableCheck(ActiveCheck<?, ?> check)
    {
        synchronized (this)
        {
            this.schedulerActionProducer.publish(new SchedulerKey(check.getSiteId(), check.getPool()), new DisableCheck(check.toStubMO()));
        }
    }
}

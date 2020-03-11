package com.intrbiz.bergamot.ui.action;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.cluster.queue.SchedulerActionProducer;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.scheduler.DisableCheck;
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Action;

public class SchedulerActions implements BalsaAction<BergamotApp>
{

    public SchedulerActions()
    {
        super();
    }

    @Action("enable-check")
    public void enableCheck(ActiveCheck<?, ?> check)
    {
        SchedulerActionProducer producer = app().getProcessingPoolCoordinator().createSchedulerActionProducer();
        producer.publishSchedulerAction(new EnableCheck(check.getId()));
    }
    
    @Action("disable-check")
    public void disableCheck(ActiveCheck<?, ?> check)
    {
        SchedulerActionProducer producer = app().getProcessingPoolCoordinator().createSchedulerActionProducer();
        producer.publishSchedulerAction(new DisableCheck(check.getId()));
    }
}

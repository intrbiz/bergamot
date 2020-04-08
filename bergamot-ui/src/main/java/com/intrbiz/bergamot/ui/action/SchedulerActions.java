package com.intrbiz.bergamot.ui.action;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck.Command;
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
        app().getProcessor().getPoolDispatcher().dispatch(check.getPool(), new ScheduleCheck(check.getId(), Command.ENABLE));
    }
    
    @Action("disable-check")
    public void disableCheck(ActiveCheck<?, ?> check)
    {
        app().getProcessor().getPoolDispatcher().dispatch(check.getPool(), new ScheduleCheck(check.getId(), Command.DISABLE));
    }
}

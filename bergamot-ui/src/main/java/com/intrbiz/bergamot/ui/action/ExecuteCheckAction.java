package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Action;

public class ExecuteCheckAction implements BalsaAction<BergamotApp>
{
    private Logger logger = Logger.getLogger(ExecuteCheckAction.class);
    
    public ExecuteCheckAction()
    {
        super();
    }
    
    @Action("execute-check")
    public void executeCheck(ActiveCheck<?,?> check)
    {
        if (logger.isTraceEnabled())logger.trace("Manually executing check: " + check.getId());
        app().getProcessor().getScheduler().executeCheck(check);
    }
}

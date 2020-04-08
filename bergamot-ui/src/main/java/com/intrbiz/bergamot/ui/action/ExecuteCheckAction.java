package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.Accounting;
import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.accounting.model.ExecuteCheckAccountingEvent;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Action;

public class ExecuteCheckAction implements BalsaAction<BergamotApp>
{
    private Logger logger = Logger.getLogger(ExecuteCheckAction.class);
    
    private Accounting accounting = Accounting.create(ExecuteCheckAction.class);
    
    public ExecuteCheckAction()
    {
    }
    
    @Action("execute-check")
    public void executeCheck(ActiveCheck<?,?> check)
    {
        ExecuteCheck executeCheck = check.executeCheck();
        if (executeCheck != null)
        {
            if (logger.isTraceEnabled())logger.trace("Executing check:\r\n" + executeCheck);
            this.accounting.account(new ExecuteCheckAccountingEvent(executeCheck.getSiteId(), executeCheck.getId(), check.getId(), executeCheck.getEngine(), executeCheck.getExecutor(), executeCheck.getName()));
            app().getProcessor().getCheckDispatcher().dispatchCheck(executeCheck);
        }
    }
}

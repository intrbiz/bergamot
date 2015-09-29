package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.metadata.Action;

public class CheckActions
{
    private Logger logger = Logger.getLogger(CheckActions.class);
    
    public CheckActions()
    {
    }
    
    @Action("suppress-check")
    public void suppressCheck(Check<?,?> check)
    {
        if (logger.isTraceEnabled()) logger.trace("Suppressing check:\r\n" + check);
        check.suppress();
    }
    
    @Action("unsuppress-check")
    public void unsuppressCheck(Check<?,?> check)
    {
        if (logger.isTraceEnabled()) logger.trace("Unsuppressing check:\r\n" + check);
        check.unsuppress();
    }
}

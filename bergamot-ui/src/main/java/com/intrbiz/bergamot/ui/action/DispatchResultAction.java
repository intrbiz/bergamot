package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.model.message.processor.result.PassiveResult;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Action;

public class DispatchResultAction implements BalsaAction<BergamotUI>
{
    private Logger logger = Logger.getLogger(DispatchResultAction.class);
    
    public DispatchResultAction()
    {
    }
    
    @Action("dispatch-result")
    public void dispatchResult(PassiveResult resultMO)
    {
        // fire off the result
        if (logger.isTraceEnabled())
            logger.trace("Publishing passive result:\r\n" + resultMO);
        // publish the passive result to a processor
        app().getProcessor().getProcessorDispatcher().dispatchResult(resultMO);
    }
}

package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.action.BalsaAction;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Action;

public class DispatchResultAction implements BalsaAction<BergamotApp>
{
    private Logger logger = Logger.getLogger(DispatchResultAction.class);
    
    public DispatchResultAction()
    {
    }
    
    @Action("dispatch-result")
    public void dispatchResult(PassiveResultMO resultMO)
    {
        // fire off the result
        if (logger.isTraceEnabled())
            logger.trace("Publishing passive result:\r\n" + resultMO);
        // publish the passive result to a processor
        app().getProcessingPoolCoordinator()
            .createProcessingPoolProducer().publishResult(resultMO);
    }
}

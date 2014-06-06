package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.metadata.Action;
import com.intrbiz.queue.RoutedProducer;

public class ExecuteCheckAction
{
    private Logger logger = Logger.getLogger(ExecuteCheckAction.class);
    
    private WorkerQueue queue;
    
    private RoutedProducer<ExecuteCheck> executeCheckProducer;
    
    public ExecuteCheckAction()
    {
        this.queue = WorkerQueue.open();
        this.executeCheckProducer = this.queue.publishChecks();
    }
    
    @Action("execute-check")
    public void executeCheck(ActiveCheck<?,?> check)
    {
        // fire off the check
        ExecuteCheck executeCheck = check.executeCheck();
        if (executeCheck != null)
        {
            if (logger.isTraceEnabled()) logger.trace("Executing check:\r\n" + executeCheck);
            // TODO
            synchronized (this)
            {
                this.executeCheckProducer.publish(check.getRoutingKey(), executeCheck, check.getMessageTTL());
            }
        }
    }
}

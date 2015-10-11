package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.ExecuteCheckAccountingEvent;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.metadata.Action;
import com.intrbiz.queue.RoutedProducer;

public class ExecuteCheckAction
{
    private Logger logger = Logger.getLogger(ExecuteCheckAction.class);
    
    private WorkerQueue queue;
    
    private RoutedProducer<ExecuteCheck, WorkerKey> executeCheckProducer;
    
    private Accounting accounting = Accounting.create(ExecuteCheckAction.class);
    
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
            // account
            this.accounting.account(new ExecuteCheckAccountingEvent(executeCheck.getSiteId(), executeCheck.getId(), check.getId(), executeCheck.getEngine(), executeCheck.getExecutor(), executeCheck.getName()));
            // execute
            synchronized (this)
            {
                this.executeCheckProducer.publish(check.getRoutingKey(), executeCheck, check.getMessageTTL());
            }
        }
    }
}

package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ActiveResultKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.metadata.Action;
import com.intrbiz.queue.RoutedProducer;

public class DispatchResultAction
{
    private Logger logger = Logger.getLogger(DispatchResultAction.class);
    
    private WorkerQueue queue;
    
    private RoutedProducer<ResultMO, ResultKey> resultProducer;
    
    public DispatchResultAction()
    {
        this.queue = WorkerQueue.open();
        this.resultProducer = this.queue.publishResults();
    }
    
    @Action("dispatch-result")
    public void dispatchResult(ResultMO resultMO)
    {
        // fire off the result
        if (logger.isTraceEnabled()) logger.trace("Dispatching result:\r\n" + resultMO);
        // TODO
        synchronized (this)
        {
            this.resultProducer.publish(new ActiveResultKey(resultMO.getSiteId(), resultMO.getProcessingPool()), resultMO);
        }
    }
}

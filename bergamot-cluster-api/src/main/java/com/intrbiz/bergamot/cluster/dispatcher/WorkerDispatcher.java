package com.intrbiz.bergamot.cluster.dispatcher;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

/**
 * Dispatch checks to workers
 */
public interface WorkerDispatcher
{   
    PublishStatus dispatchCheck(ExecuteCheck check);
    
    PublishStatus dispatch(WorkerMessage message);
}

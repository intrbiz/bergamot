package com.intrbiz.bergamot.cluster.queue;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public interface WorkerProducer
{

    /**
     * Execute a check by assigning it to a worker
     * @param check the check to execute
     * @return if the check was successfully given to a worker
     */
    PublishStatus executeCheck(ExecuteCheck check);
    
}

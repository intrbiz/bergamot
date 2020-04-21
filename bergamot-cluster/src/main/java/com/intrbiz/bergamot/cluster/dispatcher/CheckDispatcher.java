package com.intrbiz.bergamot.cluster.dispatcher;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

/**
 * Dispatch checks to workers
 */
public interface CheckDispatcher
{   
    PublishStatus dispatchCheck(ExecuteCheck check);
}

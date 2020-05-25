package com.intrbiz.bergamot.worker.internal;

import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public interface InternalExecutor
{
    String getName();
    
    void execute(ExecuteCheck check, InternalExecutorContext context) throws Exception;
}

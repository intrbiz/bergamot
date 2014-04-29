package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.task.check.ExecuteCheck;

public interface CheckExecutor<T extends Engine> extends Executor<T>
{
    /**
     * Execute the check
     */
    Result run(ExecuteCheck executeCheck);
}

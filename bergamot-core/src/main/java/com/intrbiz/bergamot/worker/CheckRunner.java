package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.task.Check;

public interface CheckRunner extends Runner
{
    /**
     * Execute the check
     */
    Result run(Check check);
}

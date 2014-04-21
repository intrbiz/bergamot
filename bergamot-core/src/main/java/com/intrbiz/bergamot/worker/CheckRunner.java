package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.model.result.Result;
import com.intrbiz.bergamot.model.task.Check;

public interface CheckRunner extends Runner
{
    /**
     * Execute the check
     */
    Result run(Check check);
}

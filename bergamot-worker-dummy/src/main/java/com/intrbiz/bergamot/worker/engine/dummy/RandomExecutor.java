package com.intrbiz.bergamot.worker.engine.dummy;

import java.security.SecureRandom;

import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.pool.result.ActiveResult;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Execute randomised dummy checks, which changes state randomly
 */
public class RandomExecutor extends AbstractExecutor<DummyEngine>
{
    public static final String NAME = "random";
    
    private SecureRandom random = new SecureRandom();

    public RandomExecutor()
    {
        super();
    }
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return DummyEngine.NAME.equals(task.getEngine()) && NAME.equals(task.getExecutor());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        // apply a threshold to a random double from 0 - 1
        long start = System.nanoTime();
        context.publishActiveResult(
            new ActiveResult().fromCheck(executeCheck)
            .applyGreaterThanThreshold(
                    this.random.nextDouble(), 
                    executeCheck.getDoubleParameter("warning", 0.7D), 
                    executeCheck.getDoubleParameter("critical", 0.9D), 
                    executeCheck.getParameter("output", "")
            )
            .runtime(((double)(System.nanoTime() - start)) / 1_000_000D)
        );
    }
}

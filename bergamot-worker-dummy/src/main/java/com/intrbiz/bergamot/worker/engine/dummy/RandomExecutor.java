package com.intrbiz.bergamot.worker.engine.dummy;

import java.security.SecureRandom;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Execute randomised dummy checks, which changes state randomly
 */
public class RandomExecutor extends AbstractCheckExecutor<DummyEngine>
{
    public static final String NAME = "random";
    
    private SecureRandom random = new SecureRandom();

    public RandomExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        // apply a threshold to a random double from 0 - 1
        context.publishActiveResult(
            new ActiveResult()
            .applyGreaterThanThreshold(
                    this.random.nextDouble(), 
                    executeCheck.getDoubleParameter("warning", 0.7D), 
                    executeCheck.getDoubleParameter("critical", 0.9D), 
                    executeCheck.getParameter("output", "")
            )
        );
    }
}

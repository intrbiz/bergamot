package com.intrbiz.bergamot.worker.engine.dummy;

import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.pool.result.ActiveResult;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Execute static dummy checks
 */
public class StaticExecutor extends AbstractExecutor<DummyEngine>
{
    public static final String NAME = "static";

    public StaticExecutor()
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
        long start = System.nanoTime();
        ActiveResult result = new ActiveResult().fromCheck(executeCheck);
        if (Boolean.getBoolean("dummy.static.critical"))
        {
            result.setOk(false);
            result.setStatus("CRITICAL");
            result.setOutput("Something went wrong there");   
        }
        else
        {
            result.setOk(executeCheck.getBooleanParameter("ok", true));
            result.setStatus(executeCheck.getParameter("status", "OK"));
            result.setOutput(executeCheck.getParameter("output", ""));
        }
        result.setRuntime(((double)(System.nanoTime() - start)) / 1_000_000D);
        context.publishActiveResult(result);
    }
}

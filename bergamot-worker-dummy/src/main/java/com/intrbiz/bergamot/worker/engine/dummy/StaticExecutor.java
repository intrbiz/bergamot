package com.intrbiz.bergamot.worker.engine.dummy;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Execute static dummy checks
 */
public class StaticExecutor extends AbstractCheckExecutor<DummyEngine>
{
    public static final String NAME = "static";

    public StaticExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        ActiveResult result = new ActiveResult();
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
        context.publishActiveResult(result);
    }
}

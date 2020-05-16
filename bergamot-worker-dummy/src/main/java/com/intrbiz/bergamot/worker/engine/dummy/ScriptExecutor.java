package com.intrbiz.bergamot.worker.engine.dummy;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;

/**
 * Execute scripted dummy checks
 */
public class ScriptExecutor extends AbstractCheckExecutor<DummyEngine>
{
    public static final String NAME = "script";
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();

    public ScriptExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        try
        {
            // execute the script
            this.scriptManager.createExecutor(executeCheck, context)
                .execute();
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }
    }
}

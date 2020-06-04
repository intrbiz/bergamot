package com.intrbiz.bergamot.worker.engine.snmp;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;
import com.intrbiz.snmp.SNMPContext;

/**
 * Execute Scripted SNMP checks
 */
public class ScriptedSNMPExecutor extends AbstractSNMPExecutor
{   
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();

    public ScriptedSNMPExecutor()
    {
        super("script", true);
    }

    @Override
    protected void executeSNMP(ExecuteCheck executeCheck, CheckExecutionContext context, SNMPContext<?> agent) throws Exception
    {
        try
        {
            // execute the script
            this.scriptManager.createExecutor(executeCheck, context)
                .bind("agent", agent.with((error) -> context.publishActiveResult(new ActiveResult().error(error))))
                .execute();
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResult().error(e));
        }
    }
}

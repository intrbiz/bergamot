package com.intrbiz.bergamot.worker.engine.snmp;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ActiveCheckScriptContext;
import com.intrbiz.scripting.RestrictedScriptEngineManager;
import com.intrbiz.snmp.SNMPContext;

/**
 * Execute Scripted SNMP checks
 */
public class ScriptedSNMPExecutor extends AbstractSNMPExecutor
{   
    private ScriptEngineManager factory = new RestrictedScriptEngineManager();

    public ScriptedSNMPExecutor()
    {
        super();
    }

    /**
     * Where executor == 'script' or is empty
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && ("script".equalsIgnoreCase(task.getExecutor()) || Util.isEmpty(task.getExecutor())); 
    }

    @Override
    protected void executeSNMP(ExecuteCheck executeCheck, CheckExecutionContext context, SNMPContext<?> agent) throws Exception
    {
        // we need a script!
        if (Util.isEmpty(executeCheck.getScript())) throw new RuntimeException("The script must be defined!");
        // setup wrapped context
        SNMPContext<?> wrapped = agent.with((error) -> context.publishActiveResult(new ActiveResult().error(error)));
        // setup the script engine
        ScriptEngine script = factory.getEngineByName("nashorn");
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("check", executeCheck);
        bindings.put("agent", wrapped);
        bindings.put("bergamot", new ActiveCheckScriptContext(executeCheck, context));
        script.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        // execute
        script.eval(executeCheck.getScript());
    }
}

package com.intrbiz.bergamot.worker.engine.script;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.Executor;
import com.intrbiz.scripting.RestrictedScriptEngineManager;

/**
 * Easily execute a scripted Bergamot check.  This class will take care of setting up and executing 
 * the scripted check, it will bind the check and bergamot script context into the script.  All the
 * caller needs to do is bind any additional variables and call execute.  Any errors which occur 
 * will result in an error result being published.
 * 
 * public class MyExecutor
 * {
 *     private final ScriptedCheckManager scriptManager = new ScriptedCheckManager(this);
 *     
 *     public void execute(ExecuteCheck executeCheck)
 *     {
 *          this.scriptManager.createExecutor(executeCheck)
 *              .bind("some_variable", new Object())
 *              .execute();
 *     }
 * }
 *
 */
public class ScriptedCheckManager
{
    private final ScriptEngineManager factory = new RestrictedScriptEngineManager();
    
    private final Executor<?> executor;
    
    public ScriptedCheckManager(Executor<?> executor)
    {
        super();
        this.executor = executor;
    }
    
    protected ScriptEngineManager getScriptEngineManager()
    {
        return this.factory;
    }
    
    public ScriptedCheckExecutor createExecutor(final ExecuteCheck executeCheck)
    {
        // script bindings
        final SimpleBindings bindings = new SimpleBindings();
        bindings.put("check", executeCheck);
        bindings.put("bergamot", new ActiveCheckScriptContext(executeCheck, this.executor));
        // setup the executor
        return new ScriptedCheckExecutor()
        {
            @Override
            public ScriptedCheckExecutor bind(String variableName, Object value)
            {
                bindings.put(variableName, value);
                return this;
            }

            @Override
            public void execute()
            {
                try
                {
                    final String script = executeCheck.getScript();
                    // we need a script to execute
                    if (Util.isEmpty(script)) throw new BergamotScriptedCheckException("A script must be provided");
                    // create the script
                    ScriptEngine scriptEngine = ScriptedCheckManager.this.factory.getEngineByName("nashorn");
                    scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                    // execute the script
                    scriptEngine.eval(script);
                }
                catch (Exception e)
                {
                    ScriptedCheckManager.this.executor.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
                }
            }
        };
    }
}

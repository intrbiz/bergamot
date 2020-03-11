package com.intrbiz.bergamot.test;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ActiveCheckScriptContext;
import com.intrbiz.scripting.RestrictedScriptEngineManager;

public class ScriptExample
{
    static
    {
        // only setup the security manager if it is not already loaded
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        ScriptEngineManager manager = new RestrictedScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        //
        ExecuteCheck check = new ExecuteCheck();
        TestContext context = new TestContext();
        // create the script context
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("check", check);
        bindings.put("bergamot", new ActiveCheckScriptContext(check, context));
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        // execute
        engine.eval("bergamot.publishReadings([ "
                + "{ type: 'double', name: 'one', unit: '', value: 0.0 }, "
                + "{ type: 'long', name: 'two', unit: '', value: 1 } "
        + "]);");
    }
    
    public static class TestContext implements CheckExecutionContext
    {
        public void publishResult(ResultMO resultMO)
        {
            System.out.println("Result: " + resultMO);
        }
        
        public void publishReading(ReadingParcelMO readingParcelMO)
        {
            System.out.println("Readings: " + readingParcelMO);
        }
    }
}

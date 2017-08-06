package com.intrbiz.bergamot.test;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.Executor;
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
        Executor<?> exec = new TestExecutor();
        ActiveCheckScriptContext context = new ActiveCheckScriptContext(check, exec);
        //
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("check", check);
        bindings.put("bergamot", context);
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        //
        engine.eval("bergamot.publishReadings([ "
                + "{ type: 'double', name: 'one', unit: '', value: 0.0 }, "
                + "{ type: 'long', name: 'two', unit: '', value: 1 } "
        + "]);");

    }
    
    @SuppressWarnings("rawtypes")
    public static class TestExecutor extends AbstractExecutor
    {
        public void publishResult(ResultKey key, ResultMO resultMO)
        {
            System.out.println("Result: " + key + " => " + resultMO);
        }
        
        public void publishReading(ReadingKey key, ReadingParcelMO readingParcelMO)
        {
            System.out.println("Readings: " + key + " => " + readingParcelMO);
        }

        @Override
        public boolean accept(ExecuteCheck task)
        {
            return false;
        }

        @Override
        public void execute(ExecuteCheck executeCheck)
        {            
        }

        @Override
        public void configure(ExecutorCfg cfg) throws Exception
        {   
        }
    }
}

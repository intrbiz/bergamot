package com.intrbiz.bergamot.worker.engine.jmx;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.scripting.RestrictedScriptEngineManager;

/**
 * 
 */
public class ScriptedJMXExecutor extends AbstractExecutor<JMXEngine>
{
    public static final String NAME = "script";
    
    private Logger logger = Logger.getLogger(ScriptedJMXExecutor.class);
    
    private ScriptEngineManager factory = new RestrictedScriptEngineManager();
    
    public ScriptedJMXExecutor()
    {
        super();
    }
    
    /**
     * Only execute Checks where the engine == "jmx" and executor == "script"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return JMXEngine.NAME.equalsIgnoreCase(task.getEngine()) &&
               ScriptedJMXExecutor.NAME.equalsIgnoreCase(task.getExecutor());
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck)
    {
        try
        {
            // we need a script!
            if (Util.isEmpty(executeCheck.getScript())) throw new RuntimeException("The script must be defined!");
            // setup the script engine
            ScriptEngine script = factory.getEngineByName("nashorn");
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("check", executeCheck);
            bindings.put("jmx", this.getEngine().getChecker().createContext((t) -> {
                this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(t));
            }));
            bindings.put("bergamot", this.createScriptContext(executeCheck));
            script.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            // execute
            script.eval(executeCheck.getScript());
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
}

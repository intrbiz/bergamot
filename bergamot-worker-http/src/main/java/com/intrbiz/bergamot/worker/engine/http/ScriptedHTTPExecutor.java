package com.intrbiz.bergamot.worker.engine.http;

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
 * Execute HTTP check scripts, a Javascript script can be provided to 
 * setup and process a HTTP based check, for example this could be 
 * fetching an API value.
 * 
 */
public class ScriptedHTTPExecutor extends AbstractExecutor<HTTPEngine>
{
    public static final String NAME = "script";
    
    private Logger logger = Logger.getLogger(ScriptedHTTPExecutor.class);
    
    private ScriptEngineManager factory = new RestrictedScriptEngineManager();
    
    public ScriptedHTTPExecutor()
    {
        super();
    }
    
    /**
     * Only execute Checks where the engine == "http" and executor == "script"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return HTTPEngine.NAME.equalsIgnoreCase(task.getEngine()) &&
               ScriptedHTTPExecutor.NAME.equalsIgnoreCase(task.getExecutor());
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
        bindings.put("http", this.getEngine().getChecker());
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

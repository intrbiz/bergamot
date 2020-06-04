package com.intrbiz.bergamot.worker.engine.http;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;

/**
 * Execute HTTP check scripts, a Javascript script can be provided to 
 * setup and process a HTTP based check, for example this could be 
 * fetching an API value.
 * 
 */
public class ScriptedHTTPExecutor extends AbstractCheckExecutor<HTTPEngine>
{
    public static final String NAME = "script";
    
    private Logger logger = Logger.getLogger(ScriptedHTTPExecutor.class);
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();
    
    public ScriptedHTTPExecutor()
    {
        super(NAME);
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck, final CheckExecutionContext context)
    {
        try
        {
            // execute the script
            this.scriptManager.createExecutor(executeCheck, context)
                .bind("http", this.getEngine().getChecker())
                .execute();
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            context.publishActiveResult(new ActiveResult().error(e));
        }
    }
}

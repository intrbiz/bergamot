package com.intrbiz.bergamot.worker.engine.jmx;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;

/**
 * 
 */
public class ScriptedJMXExecutor extends AbstractCheckExecutor<JMXEngine>
{
    public static final String NAME = "script";
    
    private Logger logger = Logger.getLogger(ScriptedJMXExecutor.class);
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();
    
    public ScriptedJMXExecutor()
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
                .bind("jmx", this.getEngine().getChecker().createContext((t) -> {
                    context.publishActiveResult(new ActiveResult().error(t));
                }))
                .execute();
        }
        catch (Exception e)
        {
            if (logger.isTraceEnabled()) logger.trace("Error executing check", e);
            context.publishActiveResult(new ActiveResult().error(e));
        }
    }
}

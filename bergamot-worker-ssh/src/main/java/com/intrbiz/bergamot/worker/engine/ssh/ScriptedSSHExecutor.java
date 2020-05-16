package com.intrbiz.bergamot.worker.engine.ssh;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;

public class ScriptedSSHExecutor extends AbstractCheckExecutor<SSHEngine>
{
    public static final String NAME = "script";
    
    private Logger logger = Logger.getLogger(ScriptedSSHExecutor.class);
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();
    
    public ScriptedSSHExecutor()
    {
        super(NAME);
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck, final CheckExecutionContext checkContext)
    {
        try
        {
            // execute the script
            this.scriptManager.createExecutor(executeCheck, checkContext)
                .bind("ssh", this.getEngine().getChecker().createContext((t) -> {
                    checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(t));
                }))
                .execute();
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }
    }
}

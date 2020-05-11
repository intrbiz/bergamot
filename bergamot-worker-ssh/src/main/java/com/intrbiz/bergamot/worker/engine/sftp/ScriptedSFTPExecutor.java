package com.intrbiz.bergamot.worker.engine.sftp;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.check.ssh.SSHCheckContext;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ActiveCheckScriptContext;
import com.intrbiz.bergamot.worker.engine.ssh.util.SSHCheckUtil;
import com.intrbiz.scripting.RestrictedScriptEngineManager;

/**
 * 
 */
public class ScriptedSFTPExecutor extends AbstractExecutor<SFTPEngine>
{
    public static final String NAME = "script";
    
    private Logger logger = Logger.getLogger(ScriptedSFTPExecutor.class);
    
    private ScriptEngineManager factory = new RestrictedScriptEngineManager();
    
    public ScriptedSFTPExecutor()
    {
        super();
    }
    
    /**
     * Only execute Checks where the engine == "sftp" and executor == "script"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return SFTPEngine.NAME.equalsIgnoreCase(task.getEngine()) &&
               ScriptedSFTPExecutor.NAME.equalsIgnoreCase(task.getExecutor());
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck, final CheckExecutionContext checkContext)
    {
        try
        {
            // validate the task
            SSHCheckUtil.validateSSHParameters(executeCheck);
            // we need a script!
            if (Util.isEmpty(executeCheck.getScript())) throw new RuntimeException("The script must be defined!");
            // setup the script engine
            final ScriptEngine script = factory.getEngineByName("nashorn");
            final SimpleBindings bindings = new SimpleBindings();
            bindings.put("check", executeCheck);
            bindings.put("ssh", this.getEngine().getChecker().createContext((t) -> {
                checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(t));
            }));
            bindings.put("bergamot", new ActiveCheckScriptContext(executeCheck, checkContext));
            script.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            // create our context
            SSHCheckContext context = this.getEngine().getChecker().createContext((e) -> { 
                checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e)); 
            });
            // setup the context - this will add SSH keys etc
            SSHCheckUtil.setupSSHCheckContext(executeCheck, context);
            // connect to the host
            context.connect(SSHCheckUtil.getSSHUsername(executeCheck), SSHCheckUtil.getSSHHost(executeCheck), SSHCheckUtil.getSSHPort(executeCheck), (session) -> {
                session.sftp((sftp) -> {
                    try
                    {
                        bindings.put("sftp", sftp);
                        // execute the SFTP script
                        script.eval(executeCheck.getScript());
                    }
                    catch (Exception e)
                    {
                        checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
                    }
                });                
            });
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }
    }
}

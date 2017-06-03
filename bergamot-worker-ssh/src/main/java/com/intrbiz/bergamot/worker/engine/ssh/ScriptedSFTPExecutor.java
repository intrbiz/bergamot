package com.intrbiz.bergamot.worker.engine.ssh;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.check.ssh.SSHCheckContext;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.scripting.RestrictedScriptEngineManager;

/**
 * 
 */
public class ScriptedSFTPExecutor extends AbstractExecutor<SSHEngine>
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
        return SSHEngine.SFTP_NAME.equalsIgnoreCase(task.getEngine()) &&
               ScriptedSFTPExecutor.NAME.equalsIgnoreCase(task.getExecutor());
    }
    
    @Override
    public void execute(final ExecuteCheck executeCheck)
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
                this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(t));
            }));
            bindings.put("bergamot", this.createScriptContext(executeCheck));
            script.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            // create our context
            SSHCheckContext context = this.getEngine().getChecker().createContext((e) -> { 
                this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e)); 
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
                        this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
                    }
                });                
            });
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
}

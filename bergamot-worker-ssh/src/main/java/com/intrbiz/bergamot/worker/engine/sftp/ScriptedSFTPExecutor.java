package com.intrbiz.bergamot.worker.engine.sftp;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.check.ssh.SSHCheckContext;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;
import com.intrbiz.bergamot.worker.engine.ssh.util.SSHCheckUtil;

public class ScriptedSFTPExecutor extends AbstractCheckExecutor<SFTPEngine>
{
    public static final String NAME = "script";
    
    private Logger logger = Logger.getLogger(ScriptedSFTPExecutor.class);
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();
    
    public ScriptedSFTPExecutor()
    {
        super(NAME);
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
                        // execute the script
                        this.scriptManager.createExecutor(executeCheck, checkContext)
                            .bind("ssh", this.getEngine().getChecker().createContext((t) -> {
                                checkContext.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(t));
                            }))
                            .bind("sftp", sftp)
                            .execute();
                    }
                    catch (Exception e)
                    {
                        checkContext.publishActiveResult(new ActiveResult().error(e));
                    }
                });                
            });
        }
        catch (Exception e)
        {
            logger.error("Error executing check", e);
            checkContext.publishActiveResult(new ActiveResult().error(e));
        }
    }
}

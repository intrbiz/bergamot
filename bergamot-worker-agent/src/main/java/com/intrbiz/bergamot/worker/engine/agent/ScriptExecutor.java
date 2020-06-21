package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgent;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.agent.script.BergamotAgentScriptWrapper;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;

/**
 * Execute a scripted Bergamot check against an agent
 */
public class ScriptExecutor extends AbstractCheckExecutor<AgentEngine>
{
    public static final String NAME = "script";
    
    private static final Logger logger = Logger.getLogger(ScriptExecutor.class);
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();

    public ScriptExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Running script Bergamot Agent check");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgent agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // execute the script
                this.scriptManager.createExecutor(executeCheck, context)
                    .bind("agent", new BergamotAgentScriptWrapper(agent))
                    .execute();
            }
            else
            {
                // raise an error
                context.publishActiveResult(new ActiveResult().disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResult().error(e));
        }
    }
    
}

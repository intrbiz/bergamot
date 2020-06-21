package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import com.intrbiz.bergamot.agent.server.BergamotAgent;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

public abstract class AbstractAgentExecutor<C extends Message, S extends Message> extends AbstractCheckExecutor<AgentEngine>
{
    public AbstractAgentExecutor(String name)
    {
        super(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgent agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // get the Agent stats
                agent.sendMessageToAgent(this.buildRequest(executeCheck, context), (response) -> {
                    if (response instanceof GeneralError)
                    {
                        GeneralError error = (GeneralError) response;
                        context.publishActiveResult(new ActiveResult().error(error.getMessage()));
                    }
                    else
                    {
                        try
                        {
                            this.processResponse((S) response, executeCheck, context);
                        }
                        catch (Exception e)
                        {
                            context.publishActiveResult(new ActiveResult().error(e));                
                        }
                    }
                });
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
    
    protected abstract C buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context);
    
    protected abstract void processResponse(S response, ExecuteCheck executeCheck, CheckExecutionContext context);
}

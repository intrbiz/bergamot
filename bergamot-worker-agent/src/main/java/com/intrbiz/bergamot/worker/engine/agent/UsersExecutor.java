package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.stat.WhoStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;


/**
 * Check active users of a Bergamot Agent
 */
public class UsersExecutor extends AbstractCheckExecutor<AgentEngine>
{
    public static final String NAME = "users";
    
    private Logger logger = Logger.getLogger(UsersExecutor.class);

    public UsersExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent active users");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // get the user stats
                agent.sendMessageToAgent(new CheckWho(), (response) -> {
                    WhoStat stat = (WhoStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got who: " + stat);
                    // apply the check
                    context.publishActiveResult(new ActiveResult().applyGreaterThanThreshold(
                            stat.getUsers().size(), 
                            executeCheck.getIntParameter("warning",  2), 
                            executeCheck.getIntParameter("critical", 5), 
                            stat.getUsers().size() + " active users"
                    ));
                    // readings
                    context.publishReading(executeCheck, new IntegerGaugeReading("active-users", null, stat.getUsers().size(), executeCheck.getIntParameter("warning",  2), executeCheck.getIntParameter("critical", 5), 0, Integer.MAX_VALUE));
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
}

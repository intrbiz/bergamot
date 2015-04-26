package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.stat.WhoStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;


/**
 * Check active users of a Bergamot Agent
 */
public class UsersExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "users";
    
    private Logger logger = Logger.getLogger(UsersExecutor.class);

    public UsersExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "agent"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && AgentEngine.NAME.equals(task.getEngine()) && NAME.equals(task.getExecutor());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent active users");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the user stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(new CheckWho(), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    WhoStat stat = (WhoStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got who in " + runtime + "ms: " + stat);
                    // apply the check
                    resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).applyGreaterThanThreshold(
                            stat.getUsers().size(), 
                            executeCheck.getIntParameter("warning",  2), 
                            executeCheck.getIntParameter("critical", 5), 
                            stat.getUsers().size() + " active users"
                    ).runtime(runtime));
                });
            }
            else
            {
                // raise an error
                resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
}

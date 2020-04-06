package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.stat.OSStat;
import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.pool.result.ActiveResult;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Display the OS an Agent is running
 */
public class OSExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "os";
    
    private Logger logger = Logger.getLogger(OSExecutor.class);

    public OSExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "agent"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return AgentEngine.NAME.equals(task.getEngine()) && NAME.equals(task.getExecutor());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Getting Bergamot Agent OS information");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // get the CPU stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(new CheckOS(), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    OSStat stat = (OSStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got OS info in " + runtime + "ms: " + stat);
                    // display the info
                    context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).info(
                            "OS: " + stat.getVendor() + " " + stat.getVendorVersion() + ", " + stat.getName() + " " + stat.getVersion() + " (" + stat.getMachine() + ")"
                    ));
                });
            }
            else
            {
                // raise an error
                context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }
    }
}

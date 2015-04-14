package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.stat.ExecStat;
import com.intrbiz.bergamot.model.message.agent.util.Parameter;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;


/**
 * Execute a Nagios plugin via the Bergamot Agent
 */
public class NagiosExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "nagios";
    
    private Logger logger = Logger.getLogger(NagiosExecutor.class);

    public NagiosExecutor()
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
        if (logger.isTraceEnabled()) logger.trace("Executing Nagios plugin via Bergamot Agent");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // command line
            String commandLine = executeCheck.getParameter("command_line");
            if (Util.isEmpty(commandLine)) throw new RuntimeException("The command_line must be defined!");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                ExecCheck check = new ExecCheck();
                check.setName(executeCheck.getName());
                check.setEngine("nagios");
                check.getParameters().add(new Parameter("command_line", commandLine));
                // exec the check
                long sent = System.nanoTime();
                agent.sendMessageToAgent(check, (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    ExecStat stat = (ExecStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Executed check " + runtime + "ms: " + stat);
                    // submit the result
                    ActiveResultMO result = new ActiveResultMO().fromCheck(executeCheck);
                    result.setOk(stat.isOk());
                    result.setStatus(stat.getStatus());
                    result.setOutput(stat.getOutput());
                    result.runtime(runtime);
                    resultSubmitter.accept(result);
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

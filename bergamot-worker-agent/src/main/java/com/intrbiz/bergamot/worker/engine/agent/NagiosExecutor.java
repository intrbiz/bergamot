package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.stat.ExecStat;
import com.intrbiz.bergamot.model.message.agent.util.Parameter;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;



/**
 * Execute a Nagios plugin via the Bergamot Agent
 */
public class NagiosExecutor extends AbstractCheckExecutor<AgentEngine>
{
    public static final String NAME = "nagios";
    
    private Logger logger = Logger.getLogger(NagiosExecutor.class);

    public NagiosExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
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
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                ExecCheck check = new ExecCheck();
                check.setName(executeCheck.getName());
                check.setEngine("nagios");
                check.getParameters().add(new Parameter("command_line", commandLine));
                // exec the check
                agent.sendMessageToAgent(check, (response) -> {
                    ExecStat stat = (ExecStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Executed check: " + stat);
                    // submit the result
                    ActiveResult result = new ActiveResult();
                    result.setOk(stat.isOk());
                    result.setStatus(stat.getStatus());
                    result.setOutput(stat.getOutput());
                    context.publishActiveResult(result);
                    // readings
                    if (stat.getReadings().size() > 0)
                    {
                        ReadingParcelMessage readings = new ReadingParcelMessage().fromCheck(executeCheck.getCheckId());
                        readings.setCaptured(System.currentTimeMillis());
                        readings.getReadings().addAll(stat.getReadings());
                        context.publishReading(readings);
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
}

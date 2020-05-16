package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.stat.ProcessStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;



/**
 * Check processes running on a Bergamot Agent
 */
public class ProcessesExecutor extends AbstractCheckExecutor<AgentEngine>
{
    public static final String NAME = "processes";
    
    private Logger logger = Logger.getLogger(ProcessesExecutor.class);

    public ProcessesExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent processes");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // build the check
                CheckProcess check = new CheckProcess();
                check.setFlattenCommand(executeCheck.getBooleanParameter("flatten", false));
                check.setRegex(executeCheck.getBooleanParameter("regex", false));
                check.setListProcesses(true);
                check.setTitle(executeCheck.getParameter("title"));
                check.setCommand(executeCheck.getParameter("command"));
                check.setUser(executeCheck.getParameter("user"));
                check.setGroup(executeCheck.getParameter("group"));
                check.setState(executeCheck.getParameterCSV("state").stream().collect(Collectors.toList()));
                check.setArguments(executeCheck.getParametersStartingWithValues("argument"));
                // get the process stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(check, (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    ProcessStat stat = (ProcessStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got processes in " + runtime + "ms: " + stat);
                    // apply the check
                    context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).applyRange(
                            stat.getProcesses().size(),
                            executeCheck.getIntRangeParameter("warning",  new Integer[] {1, 1}), 
                            executeCheck.getIntRangeParameter("critical", new Integer[] {1, 1}), 
                            "found " + stat.getProcesses().size() + " processes"
                    ).runtime(runtime));
                    // readings
                    context.publishReading(executeCheck, new IntegerGaugeReading("processes", null, stat.getProcesses().size()));
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

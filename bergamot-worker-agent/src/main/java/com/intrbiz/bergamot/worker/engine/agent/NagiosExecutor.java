package com.intrbiz.bergamot.worker.engine.agent;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.stat.ExecStat;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;



/**
 * Execute a Nagios plugin via the Bergamot Agent
 */
public class NagiosExecutor extends AbstractAgentExecutor<ExecCheck, ExecStat>
{
    public static final String NAME = "nagios";
    
    private Logger logger = Logger.getLogger(NagiosExecutor.class);

    public NagiosExecutor()
    {
        super(NAME);
    }

    @Override
    protected ExecCheck buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        // command line
        String commandLine = executeCheck.getParameter("command_line");
        if (Util.isEmpty(commandLine)) throw new RuntimeException("The command_line must be defined!");
        // exec
        ExecCheck check = new ExecCheck();
        check.setName(executeCheck.getName());
        check.setEngine("nagios");
        check.getParameters().add(new ParameterMO("command_line", commandLine));
        return check;
    }

    @Override
    protected void processResponse(ExecStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
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
    }
}

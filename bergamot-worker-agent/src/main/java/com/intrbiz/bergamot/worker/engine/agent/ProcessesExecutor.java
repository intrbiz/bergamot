package com.intrbiz.bergamot.worker.engine.agent;

import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.stat.ProcessStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;



/**
 * Check processes running on a Bergamot Agent
 */
public class ProcessesExecutor extends AbstractAgentExecutor<CheckProcess, ProcessStat>
{
    public static final String NAME = "processes";
    
    private Logger logger = Logger.getLogger(ProcessesExecutor.class);

    public ProcessesExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckProcess buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
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
        return check;
    }

    @Override
    protected void processResponse(ProcessStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got processes: " + stat);
        // apply the check
        context.publishActiveResult(new ActiveResult().applyRange(
                stat.getProcesses().size(),
                executeCheck.getIntRangeParameter("warning",  new Integer[] {1, 1}), 
                executeCheck.getIntRangeParameter("critical", new Integer[] {1, 1}), 
                "found " + stat.getProcesses().size() + " processes"
        ));
        // readings
        context.publishReading(executeCheck, new IntegerGaugeReading("processes", null, stat.getProcesses().size()));
    }
}

package com.intrbiz.bergamot.worker.engine.agent;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.stat.WhoStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;


/**
 * Check active users of a Bergamot Agent
 */
public class UsersExecutor extends AbstractAgentExecutor<CheckWho, WhoStat>
{
    public static final String NAME = "users";
    
    private Logger logger = Logger.getLogger(UsersExecutor.class);

    public UsersExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckWho buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckWho();
    }

    @Override
    protected void processResponse(WhoStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
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
    }
}

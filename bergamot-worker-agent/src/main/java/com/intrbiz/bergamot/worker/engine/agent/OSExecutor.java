package com.intrbiz.bergamot.worker.engine.agent;

import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.stat.OSStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Display the OS an Agent is running
 */
public class OSExecutor extends AbstractAgentExecutor<CheckOS, OSStat>
{
    public static final String NAME = "os";

    public OSExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckOS buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckOS();
    }

    @Override
    protected void processResponse(OSStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        // display the info
        context.publishActiveResult(new ActiveResult().info(
                "OS: " + stat.getVendor() + " " + stat.getVendorVersion() + ", " + stat.getName() + " " + stat.getVersion() + " (" + stat.getMachine() + ")"
        ));
    }
}

package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckMetrics;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.MetricsStat;
import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.pool.result.ActiveResult;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;

/**
 * Check some metrics retrieved from an Agent
 */
public class MetricsExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "metrics";
    
    private static final Logger logger = Logger.getLogger(MetricsExecutor.class);
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();

    public MetricsExecutor()
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
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent Metrics");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // What metrics should we get
                CheckMetrics checkMetrics = new CheckMetrics();
                // metric name filter
                String metricNameFilter = executeCheck.getParameter("metric_name_filter");
                if (! Util.isEmpty(metricNameFilter)) checkMetrics.setMetricsNameFilter(metricNameFilter);
                // strip host from metric name
                checkMetrics.setStripSourceFromMetricName(executeCheck.getBooleanParameter("strip_source_from_metric_name", true));
                // send to agent
                long sent = System.nanoTime();
                agent.sendMessageToAgent(checkMetrics, (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    // error from agent?
                    if (response instanceof GeneralError)
                    {
                        context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(((GeneralError) response).getMessage()));
                        return;
                    }
                    // process the stat
                    MetricsStat stat = (MetricsStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got metrics from agent in " + runtime + "ms: " + stat);
                    // if we have a script execute it otherwise just publish an informational result
                    if (Util.isEmpty(executeCheck.getScript()))
                    {
                        context.publishActiveResult( 
                                new ActiveResult().fromCheck(executeCheck)
                                 .runtime(runtime)
                                 .info("Got " + stat.getReadings().size() + " metric readings")
                        );
                    }
                    else
                    {
                        this.scriptManager.createExecutor(executeCheck, context)
                            .bind("runtime", runtime)
                            .bind("metrics", stat.getReadings())
                            .bind("readings", stat.getReadings())
                            .execute();
                    }
                    // finally publish all readings we got
                    context.publishReading(executeCheck, stat.getReadings());
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

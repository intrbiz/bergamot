package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckMetrics;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.MetricsStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.script.ScriptedCheckManager;

/**
 * Check some metrics retrieved from an Agent
 */
public class MetricsExecutor extends AbstractCheckExecutor<AgentEngine>
{
    public static final String NAME = "metrics";
    
    private static final Logger logger = Logger.getLogger(MetricsExecutor.class);
    
    private final ScriptedCheckManager scriptManager = new ScriptedCheckManager();

    public MetricsExecutor()
    {
        super(NAME);
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
                agent.sendMessageToAgent(checkMetrics, (response) -> {
                    // error from agent?
                    if (response instanceof GeneralError)
                    {
                        context.publishActiveResult(new ActiveResult().error(((GeneralError) response).getMessage()));
                        return;
                    }
                    // process the stat
                    MetricsStat stat = (MetricsStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got metrics from agent: " + stat);
                    // if we have a script execute it otherwise just publish an informational result
                    if (Util.isEmpty(executeCheck.getScript()))
                    {
                        context.publishActiveResult( 
                                new ActiveResult().info("Got " + stat.getReadings().size() + " metric readings")
                        );
                    }
                    else
                    {
                        this.scriptManager.createExecutor(executeCheck, context)
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
                context.publishActiveResult(new ActiveResult().disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResult().error(e));
        }
    }
    
}

package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

/**
 * Fetch a batch of metrics from this agent
 */
@JsonTypeName("bergamot.agent.check.metrics")
public class CheckMetrics extends AgentMessage
{
    @JsonProperty("metric_name_filter")
    private String metricsNameFilter = "#";
    
    @JsonProperty("strip_source_from_key")
    private boolean stripSourceFromKey = true;
    
    public CheckMetrics()
    {
        super();
    }

    public CheckMetrics(AgentMessage message)
    {
        super(message);
    }

    public CheckMetrics(String id)
    {
        super(id);
    }

    public String getMetricsNameFilter()
    {
        return metricsNameFilter;
    }

    public void setMetricsNameFilter(String metricsNameFilter)
    {
        this.metricsNameFilter = metricsNameFilter;
    }

    public boolean isStripSourceFromKey()
    {
        return stripSourceFromKey;
    }

    public void setStripSourceFromKey(boolean stripSourceFromKey)
    {
        this.stripSourceFromKey = stripSourceFromKey;
    }
}

package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.gerald.polyakov.Reading;

/**
 * A set of metrics which were fetched
 */
@JsonTypeName("bergamot.agent.stat.metrics")
public class MetricsStat extends AgentMessage
{    
    /**
     * A collection of metric readings
     */
    @JsonProperty("readings")
    private List<Reading> readings = new LinkedList<Reading>();

    public MetricsStat()
    {
        super();
    }

    public MetricsStat(AgentMessage message)
    {
        super(message);
    }

    public MetricsStat(String id)
    {
        super(id);
    }

    public List<Reading> getReadings()
    {
        return readings;
    }

    public void setReadings(List<Reading> readings)
    {
        this.readings = readings;
    }
}

package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.gerald.polyakov.Reading;

/**
 * A set of metrics which were fetched
 */
@JsonTypeName("bergamot.agent.stat.metrics")
public class MetricsStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    /**
     * A collection of metric readings
     */
    @JsonProperty("readings")
    private List<Reading> readings = new LinkedList<Reading>();

    public MetricsStat()
    {
        super();
    }

    public MetricsStat(Message message)
    {
        super(message);
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

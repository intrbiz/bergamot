package com.intrbiz.bergamot.model.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.stats.check")
public class CheckStatsMO extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    // stats

    @JsonProperty("last_runtime")
    private double lastRuntime;

    @JsonProperty("average_runtime")
    private double averageRuntime;

    @JsonProperty("last_check_execution_latency")
    private double lastCheckExecutionLatency;

    @JsonProperty("average_check_execution_latency")
    private double averageCheckExecutionLatency;

    @JsonProperty("last_check_processing_latency")
    private double lastCheckProcessingLatency;

    @JsonProperty("average_check_processing_latency")
    private double averageCheckProcessingLatency;

    public CheckStatsMO()
    {
        super();
    }

    public double getLastRuntime()
    {
        return lastRuntime;
    }

    public void setLastRuntime(double lastRuntime)
    {
        this.lastRuntime = lastRuntime;
    }

    public double getAverageRuntime()
    {
        return averageRuntime;
    }

    public void setAverageRuntime(double averageRuntime)
    {
        this.averageRuntime = averageRuntime;
    }

    public double getLastCheckExecutionLatency()
    {
        return lastCheckExecutionLatency;
    }

    public void setLastCheckExecutionLatency(double lastCheckExecutionLatency)
    {
        this.lastCheckExecutionLatency = lastCheckExecutionLatency;
    }

    public double getAverageCheckExecutionLatency()
    {
        return averageCheckExecutionLatency;
    }

    public void setAverageCheckExecutionLatency(double averageCheckExecutionLatency)
    {
        this.averageCheckExecutionLatency = averageCheckExecutionLatency;
    }

    public double getLastCheckProcessingLatency()
    {
        return lastCheckProcessingLatency;
    }

    public void setLastCheckProcessingLatency(double lastCheckProcessingLatency)
    {
        this.lastCheckProcessingLatency = lastCheckProcessingLatency;
    }

    public double getAverageCheckProcessingLatency()
    {
        return averageCheckProcessingLatency;
    }

    public void setAverageCheckProcessingLatency(double averageCheckProcessingLatency)
    {
        this.averageCheckProcessingLatency = averageCheckProcessingLatency;
    }
}

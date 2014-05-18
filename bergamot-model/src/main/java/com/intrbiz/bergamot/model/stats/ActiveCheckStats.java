package com.intrbiz.bergamot.model.stats;

public class ActiveCheckStats
{
    private double lastRuntime;

    private double averageRuntime;

    private double lastCheckExecutionLatency;

    private double averageCheckExecutionLatency;

    private double lastCheckProcessingLatency;

    private double averageCheckProcessingLatency;

    public ActiveCheckStats()
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

    public double getAverageRuntime()
    {
        return averageRuntime;
    }

    public void setAverageRuntime(double averageRuntime)
    {
        this.averageRuntime = averageRuntime;
    }
}

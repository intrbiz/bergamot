package com.intrbiz.bergamot.agent.statsd;

/**
 * A metric consumer which merely logs the metric to stdout
 */
public final class LoggingConsumer implements StatsDMetricConsumer
{        
    public void processMetric(StatsDMetric metric)
    {
        System.out.println(metric);
    }
}
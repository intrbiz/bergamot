package com.intrbiz.bergamot.agent.statsd;

/**
 * A consumer of metrics which are received
 */
public interface StatsDMetricConsumer
{
    /**
     * Process the given metric
     */
    void processMetric(StatsDMetric metric);
}
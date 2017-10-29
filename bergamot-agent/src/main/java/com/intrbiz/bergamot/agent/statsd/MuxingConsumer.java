package com.intrbiz.bergamot.agent.statsd;

/**
 * A consumer which dispatches to multiple consumers
 */
public final class MuxingConsumer implements StatsDMetricConsumer
{   
    private final StatsDMetricConsumer[] consumers;
    
    public MuxingConsumer(StatsDMetricConsumer... consumers)
    {
        this.consumers = consumers;
    }
    
    public void processMetric(StatsDMetric metric)
    {
        for (StatsDMetricConsumer consumer : this.consumers)
        {
            consumer.processMetric(metric);
        }
    }
}
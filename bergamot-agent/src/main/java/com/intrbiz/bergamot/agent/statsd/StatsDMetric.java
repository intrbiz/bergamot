package com.intrbiz.bergamot.agent.statsd;

/**
 * A StatsD metric
 */
public class StatsDMetric
{    
    private final String source;
    
    private final String metricName;
    
    private final StatsDMetricType metricType;
    
    private final String metricValue;
    
    private final String sampleRate;

    public StatsDMetric(String source, String metricName, String metricValue, StatsDMetricType metricType, String sampleRate)
    {
        super();
        this.source = source;
        this.metricName = metricName;
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.sampleRate = sampleRate;
    }
    
    public StatsDMetric(String source, String metricName, String metricValue, StatsDMetricType metricType)
    {
        this(source, metricName, metricValue, metricType, null);
    }

    public String getSource()
    {
        return source;
    }

    public String getMetricName()
    {
        return metricName;
    }
    
    public String getKey()
    {
        return this.source + "." + this.metricName;
    }

    public StatsDMetricType getMetricType()
    {
        return metricType;
    }

    public String getMetricValue()
    {
        return metricValue;
    }

    public String getSampleRate()
    {
        return sampleRate;
    }
    
    public String toString()
    {
        return "[" + this.source + "]: " + this.metricName + ":" + this.metricValue + "|" + this.metricType.getEncoded() + (this.sampleRate != null ? "|@" + this.sampleRate : "");
    }
}

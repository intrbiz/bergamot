package com.intrbiz.bergamot.agent.statsd;

public enum StatsDMetricType
{
    COUNTER("c"),
    TIMER_MS("ms"),
    GAUGE("g"),
    SET("s"),
    HISTOGRAM("h");
    
    private final String encoded;
    
    private StatsDMetricType(String encoded)
    {
        this.encoded = encoded;
    }
    
    public String getEncoded()
    {
        return this.encoded;
    }
    
    public static StatsDMetricType fromEncoded(String encoded) throws StatsDException
    {
        for (StatsDMetricType value : StatsDMetricType.values())
        {
            if (value.encoded.equals(encoded))
                return value;
        }
        throw new StatsDException("Invalid metric type: " + encoded);
    }
}

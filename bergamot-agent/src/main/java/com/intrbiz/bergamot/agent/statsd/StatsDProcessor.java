package com.intrbiz.bergamot.agent.statsd;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class StatsDProcessor implements StatsDMetricConsumer
{
    private static final Logger logger = Logger.getLogger(StatsDProcessor.class);
    
    private final MetricRegistry metrics = new MetricRegistry();
    
    private final ConcurrentMap<String, Long> metricUpdatedAt = new ConcurrentHashMap<String, Long>();
    
    private final ConcurrentMap<String, DoubleGauge> gauges = new ConcurrentHashMap<String, DoubleGauge>();
    
    private final long staleMetricThresholdNanos;
    
    private final Meter metricsProcessed;
    
    public StatsDProcessor(long staleMetricThreshold, TimeUnit unit)
    {
        super();
        this.staleMetricThresholdNanos = unit.toNanos(staleMetricThreshold);
        this.metricsProcessed = this.metrics.meter("agent.statsd.metrics.processed");
    }
    
    public StatsDProcessor()
    {
        this(1, TimeUnit.HOURS);
    }
    
    public MetricRegistry getMetrics()
    {
        return this.metrics;
    }

    @Override
    public void processMetric(StatsDMetric metric)
    {
        try
        {
            this.metricsProcessed.mark();
            switch (metric.getMetricType())
            {
                case COUNTER:
                    this.processCounter(metric);
                    break;
                case TIMER_MS:
                    this.processTimer(metric, TimeUnit.MILLISECONDS);
                    break;
                case GAUGE:
                    this.processGauge(metric);
                    break;
                case SET:
                    this.processSet(metric);
                    break;
                case HISTOGRAM:
                    this.processHistogram(metric);
                    break;
            }
        }
        catch (Exception e)
        {
            logger.warn("Error processing metric " + metric, e);
        }
    }
    
    /**
     * Increment a counter
     */
    protected void processCounter(StatsDMetric metric)
    {
        String key = metric.getKey();
        // get or create the counter
        Meter meter = this.metrics.meter(key);
        // sampling
        double sampleRate = metric.getSampleRate() != null ? Double.parseDouble(metric.getSampleRate()) : 1D;
        // update the counter
        double value = Double.parseDouble(metric.getMetricValue());
        meter.mark((long) (value * (1D / sampleRate)));
        // track that we've updated this metric
        this.metricUpdated(key);
    }
    
    /**
     * Update a timer metric
     */
    protected void processTimer(StatsDMetric metric, TimeUnit unit)
    {
        String key = metric.getKey();
        // get or create the timer
        Timer timer = this.metrics.timer(key);
        // update the timer
        double value = Double.parseDouble(metric.getMetricValue());
        timer.update((long) value, unit);
        // track that we've updated this metric
        this.metricUpdated(key);
    }
    
    /**
     * Set or add to a gauge
     */
    protected void processGauge(StatsDMetric metric)
    {
        String key = metric.getKey();
        // get or create the gauge
        DoubleGauge gauge = this.gauges.get(key);
        if (gauge == null)
        {
            gauge = new DoubleGauge();
            this.gauges.putIfAbsent(key, gauge);
            this.metrics.register(key, gauge);
        }
        // get the value and the mode
        boolean add = metric.getMetricValue().startsWith("+") || metric.getMetricValue().startsWith("-");
        double value = Double.parseDouble(metric.getMetricValue());
        if (add) gauge.add(value);
        else gauge.set(value);
        // track that we've updated this metric
        this.metricUpdated(key);
    }
    
    protected void processSet(StatsDMetric metric)
    {
        // NOT SUPPORTED
    }
    
    /**
     * Track the distribution of values
     */
    protected void processHistogram(StatsDMetric metric)
    {
        String key = metric.getKey();
        // get or create the counter
        Histogram histo = this.metrics.histogram(key);
        // update the counter
        double value = Double.parseDouble(metric.getMetricValue());
        histo.update((long) value);
        // track that we've updated this metric
        this.metricUpdated(key);
    }
    
    /**
     * Keep track of when metrics where updated so that we can expunge stale metrics
     */
    protected void metricUpdated(String key)
    {
        this.metricUpdatedAt.put(key, System.nanoTime());
    }
    
    /**
     * Clear out any metrics which have not been updated for a while
     */
    public void clearUpStaleMetrics()
    {
        long now = System.nanoTime();
        for (Entry<String, Long> e : this.metricUpdatedAt.entrySet())
        {
            if ((now - e.getValue()) > this.staleMetricThresholdNanos)
            {
                this.metrics.remove(e.getKey());
                this.metricUpdatedAt.remove(e.getKey());
                this.gauges.remove(e.getKey());
            }
        }
    }
}

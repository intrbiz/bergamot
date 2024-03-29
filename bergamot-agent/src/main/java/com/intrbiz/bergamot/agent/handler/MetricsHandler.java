package com.intrbiz.bergamot.agent.handler;

import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.codahale.metrics.Metric;
import com.intrbiz.bergamot.agent.statsd.StatsDProcessor;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.agent.check.CheckMetrics;
import com.intrbiz.bergamot.model.message.agent.stat.MetricsStat;
import com.intrbiz.gerald.polyakov.Parcel;

public class MetricsHandler extends AbstractAgentHandler
{
    private static final Logger logger = Logger.getLogger(MetricsHandler.class);
    
    public MetricsHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckMetrics.class
        };
    }

    @Override
    public Message handle(Message request)
    {
        CheckMetrics metrics = (CheckMetrics) request;
        MetricsStat stat = new MetricsStat(request);
        Parcel readings = new Parcel();
        // filter the list of metrics and return the readings
        StatsDProcessor processor = this.getAgent().getStatsDProcessor();
        if (processor != null)
        {
            Pattern filter = Pattern.compile(metrics.getMetricsNameFilter());
            for (Entry<String, Metric> metric : processor.getMetrics().getMetrics().entrySet())
            {
                // strip the host from the key
                String key = metric.getKey(); 
                key = metrics.isStripSourceFromMetricName() ? key.substring(key.indexOf(".") + 1) : key;
                // does the filter match this key
                if (filter.matcher(key).matches())
                {
                    readings.addMetric(key, metric.getValue());
                }
            }
        }
        stat.setReadings(readings.getReadings());
        if (logger.isDebugEnabled()) 
            logger.debug("Sent " + stat.getReadings().size() + " readings");
        return stat;
    }
}

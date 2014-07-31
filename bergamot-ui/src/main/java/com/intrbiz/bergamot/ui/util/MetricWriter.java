package com.intrbiz.bergamot.ui.util;

import java.io.IOException;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonGenerator;

public class MetricWriter
{
    public static void writeMetric(Metric mv, JsonGenerator jg) throws IOException
    {
        jg.writeStartObject();
        //
        if (mv instanceof Gauge)
            writeGauge((Gauge<?>) mv, jg);
        else if (mv instanceof Counter)
            writeCounter((Counter) mv, jg);
        else if (mv instanceof Meter)
            writeMeter((Meter) mv, jg);
        else if (mv instanceof Timer)
            writeTimer((Timer) mv, jg);
        else if (mv instanceof Histogram) 
            writeHistogram((Histogram) mv, jg);
        //
        jg.writeEndObject();
    }

    public static void writeCounter(Counter c, JsonGenerator jg) throws IOException
    {
        jg.writeFieldName("count");
        jg.writeNumber(c.getCount());
        //
        jg.writeFieldName("type");
        jg.writeString("counter");
    }

    public static void writeMeter(Meter m, JsonGenerator jg) throws IOException
    {
        jg.writeFieldName("count");
        jg.writeNumber(m.getCount());
        //
        jg.writeFieldName("mean-rate");
        jg.writeNumber(m.getMeanRate());
        //
        jg.writeFieldName("one-minute-rate");
        jg.writeNumber(m.getOneMinuteRate());
        //
        jg.writeFieldName("five-minute-rate");
        jg.writeNumber(m.getFiveMinuteRate());
        //
        jg.writeFieldName("fifteen-minute-rate");
        jg.writeNumber(m.getFifteenMinuteRate());
        //
        jg.writeFieldName("type");
        jg.writeString("meter");
    }

    public static void writeTimer(Timer t, JsonGenerator jg) throws IOException
    {
        writeMetered(t, jg);
        writeSampling(t, jg);
        //
        jg.writeFieldName("type");
        jg.writeString("timer");
    }
    
    public static void writeCounting(Counting m, JsonGenerator jg) throws IOException
    {
        jg.writeFieldName("count");
        jg.writeNumber(m.getCount());
    }
    
    public static void writeMetered(Metered m, JsonGenerator jg) throws IOException
    {
        writeCounting(m, jg);
        //
        jg.writeFieldName("mean-rate");
        jg.writeNumber(m.getMeanRate());
        //
        jg.writeFieldName("one-minute-rate");
        jg.writeNumber(m.getOneMinuteRate());
        //
        jg.writeFieldName("five-minute-rate");
        jg.writeNumber(m.getFiveMinuteRate());
        //
        jg.writeFieldName("fifteen-minute-rate");
        jg.writeNumber(m.getFifteenMinuteRate());
    }
    
    public static void writeSampling(Sampling m, JsonGenerator jg) throws IOException
    {
        Snapshot s = m.getSnapshot();
        jg.writeFieldName("snapshot");
        jg.writeStartObject();
        //
        jg.writeFieldName("percentile-75");
        jg.writeNumber(s.get75thPercentile());
        //
        jg.writeFieldName("percentile-95");
        jg.writeNumber(s.get95thPercentile());
        //
        jg.writeFieldName("percentile-98");
        jg.writeNumber(s.get98thPercentile());
        //
        jg.writeFieldName("percentile-99");
        jg.writeNumber(s.get99thPercentile());
        //
        jg.writeFieldName("percentile-999");
        jg.writeNumber(s.get999thPercentile());
        //
        jg.writeFieldName("size");
        jg.writeNumber(s.size());
        //
        jg.writeFieldName("median");
        jg.writeNumber(s.getMedian());
        //
        jg.writeFieldName("min");
        jg.writeNumber(s.getMin());
        //
        jg.writeFieldName("mean");
        jg.writeNumber(s.getMean());
        //
        jg.writeFieldName("max");
        jg.writeNumber(s.getMax());
        //
        jg.writeFieldName("std-dev");
        jg.writeNumber(s.getStdDev());
        //
        jg.writeEndObject();
    }

    public static void writeHistogram(Histogram h, JsonGenerator jg) throws IOException
    {
        writeCounting(h, jg);
        writeSampling(h, jg);
        //
        jg.writeFieldName("type");
        jg.writeString("histogram");
    }

    public static void writeGauge(Gauge<?> g, JsonGenerator jg) throws IOException
    {
        Object val = g.getValue();
        jg.writeFieldName("value");
        if (val instanceof String)
        {
            jg.writeString((String) val);
            //
            jg.writeFieldName("value-type");
            jg.writeString("String");
        }
        else if (val instanceof Integer)
        {
            jg.writeNumber((int) val);
            //
            jg.writeFieldName("value-type");
            jg.writeString("Integer");
        }
        else if (val instanceof Long)
        {
            jg.writeNumber((long) val);
            //
            jg.writeFieldName("value-type");
            jg.writeString("Long");
        }
        else if (val instanceof Float)
        {
            jg.writeNumber((float) val);
            //
            jg.writeFieldName("value-type");
            jg.writeString("Float");
        }
        else if (val instanceof Double)
        {
            jg.writeNumber((double) val);
            //
            jg.writeFieldName("value-type");
            jg.writeString("Double");
        }
        else if (val instanceof Boolean)
        {
            jg.writeBoolean((boolean) val);
            //
            jg.writeFieldName("value-type");
            jg.writeString("Boolean");
        }
        else
        {
            jg.writeNull();
            //
            jg.writeFieldName("value-type");
            jg.writeNull();
        }
        //
        jg.writeFieldName("type");
        jg.writeString("gauge");
    }
}
package com.intrbiz.bergamot.ui.api;

import static com.intrbiz.balsa.BalsaContext.*;

import java.io.IOException;
import java.util.Map.Entry;

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
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;

@Prefix("/api/")
public class APIRouter extends Router<BergamotApp>
{   
    @Catch(BalsaNotFound.class)
    @Any("**")
    @JSON(status = HTTPStatus.NotFound)
    public String notFound()
    {
        return "Not found";
    }
    
    @Any("**")
    @Before
    @WithDataAdapter(BergamotDB.class)
    public void lookupSite(BergamotDB db)
    {
        // we want to avoid a session for the API,
        // so lookup the site
        Site site = var("site", db.getSiteByName(Balsa().request().getServerName()));
        if (site == null) throw new BalsaNotFound("No Bergamot site is configured for the server name: " + Balsa().request().getServerName());
    }
    
    //
    
    @Get("/sources")
    public void getIntelligenceSource() throws IOException
    {
        JsonGenerator j = response().json().getJsonWriter();
        j.writeStartObject();
        j.writeArrayFieldStart("sources");
        for (IntelligenceSource source : Witchcraft.get().getSources())
        {
            j.writeStartObject();
            j.writeStringField("name", source.getName());
            j.writeArrayFieldStart("metrics");
            for (Entry<String, Metric> metric : source.getRegistry().getMetrics().entrySet())
            {
                j.writeStartObject();
                // name
                j.writeStringField("name", metric.getKey());
                // metric
                j.writeFieldName("metric");
                //
                MetricWriter.writeMetric(metric.getValue(), j);
                //
                j.writeEndObject();
            }
            j.writeEndArray();
            j.writeEndObject();
        }
        j.writeEndArray();
        j.writeEndObject();
        j.flush();
    }
    
    @Get("/source/:source")
    public void getIntelligenceSource(String name) throws IOException
    {
        IntelligenceSource source = Witchcraft.get().get(name);
        if (source == null) throw new BalsaNotFound();
        //
        JsonGenerator j = response().json().getJsonWriter();
        j.writeStartObject();
        j.writeStringField("name", source.getName());
        j.writeArrayFieldStart("metrics");
        for (Entry<String, Metric> metric : source.getRegistry().getMetrics().entrySet())
        {
            j.writeStartObject();
            // name
            j.writeStringField("name", metric.getKey());
            // metric
            j.writeFieldName("metric");
            //
            MetricWriter.writeMetric(metric.getValue(), j);
            //
            j.writeEndObject();
        }
        j.writeEndArray();
        j.writeEndObject();
        j.flush();
    }
    
    @Get("/metric/:source/:name")
    public void getMetric(String sourceName, String name) throws IOException
    {
        IntelligenceSource source = Witchcraft.get().get(sourceName);
        if (source == null) throw new BalsaNotFound();
        // metric
        Metric metric = source.getRegistry().getMetrics().get(name);
        if (metric == null) throw new BalsaNotFound();
        // output
        JsonGenerator j = response().json().getJsonWriter();
        j.writeStartObject();
        j.writeStringField("name", name);
        j.writeFieldName("metric");
        MetricWriter.writeMetric(metric, j);
        j.writeEndObject();
        j.flush();
    }
    
    public static class MetricWriter
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
}

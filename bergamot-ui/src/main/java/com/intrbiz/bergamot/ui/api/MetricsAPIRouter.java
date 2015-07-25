package com.intrbiz.bergamot.ui.api;

import java.io.IOException;
import java.util.Map.Entry;

import com.codahale.metrics.Metric;
import com.fasterxml.jackson.core.JsonGenerator;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.bergamot.ui.util.MetricWriter;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;

@Prefix("/api/metrics")
@RequireValidPrincipal()
public class MetricsAPIRouter extends Router<BergamotApp>
{   
    @Get("/sources")
    @RequirePermission("api.read.system.metrics")
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
    @RequirePermission("api.read.system.metrics")
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
    @RequirePermission("api.read.system.metrics")
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
}

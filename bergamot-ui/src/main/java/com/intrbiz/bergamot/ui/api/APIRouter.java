package com.intrbiz.bergamot.ui.api;

import static com.intrbiz.balsa.BalsaContext.*;

import java.io.IOException;
import java.util.SortedMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.intrbiz.balsa.engine.impl.route.RouteMetricName;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

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
    
    @Get("/metrics")
    public void metricsJson() throws IOException
    {
        MetricsRegistry reg = Metrics.defaultRegistry();
        //
        JsonGenerator j = response().json().getJsonWriter();
        j.writeStartObject();
        j.writeArrayFieldStart("groups");
        for (Entry<String, SortedMap<MetricName, Metric>> group : reg.groupedMetrics().entrySet())
        {
            j.writeStartObject();
            j.writeStringField("group", group.getKey());
            j.writeArrayFieldStart("metrics");
            for (Entry<MetricName, Metric> metric : group.getValue().entrySet())
            {
                j.writeStartObject();
                // name
                j.writeObjectFieldStart("name");
                j.writeStringField("group", metric.getKey().getGroup());
                j.writeStringField("type", metric.getKey().getType());
                j.writeStringField("name", metric.getKey().getName());
                j.writeStringField("scope", metric.getKey().getScope());
                if (metric.getKey() instanceof RouteMetricName)
                {
                    RouteMetricName rmn = (RouteMetricName) metric.getKey();
                    j.writeStringField("method", rmn.getHttpMethod());
                    j.writeStringField("pattern", rmn.getPattern());
                }
                j.writeEndObject();
                // metric
                j.writeObjectFieldStart("metric");
                //
                if (metric.getValue() instanceof Counter)
                {
                    Counter c = (Counter) metric.getValue();
                    j.writeStringField("type", "counter");
                    j.writeNumberField("count", c.count());
                }
                else if (metric.getValue() instanceof Meter)
                {
                    Meter m = (Meter) metric.getValue();
                    j.writeStringField("type", "meter");
                    j.writeNumberField("count", m.count());
                    j.writeNumberField("mean-rate", m.meanRate());
                    j.writeNumberField("one-minute-rate", m.oneMinuteRate());
                    j.writeNumberField("five-minute-rate", m.fiveMinuteRate());
                    j.writeNumberField("fifteen-minute-rate", m.fifteenMinuteRate());
                    j.writeStringField("rate-unit", m.rateUnit().toString());
                }
                else if (metric.getValue() instanceof Timer)
                {
                    Timer t = (Timer) metric.getValue();
                    j.writeStringField("type", "timer");
                    j.writeNumberField("count", t.count());
                    //
                    j.writeNumberField("min", t.min());
                    j.writeNumberField("mean", t.mean());
                    j.writeNumberField("max", t.max());
                    j.writeStringField("duration-unit", t.durationUnit().toString());
                    //
                    j.writeNumberField("mean-rate", t.meanRate());
                    j.writeNumberField("one-minute-rate", t.oneMinuteRate());
                    j.writeNumberField("five-minute-rate", t.fiveMinuteRate());
                    j.writeNumberField("fifteen-minute-rate", t.fifteenMinuteRate());
                    j.writeStringField("rate-unit", t.rateUnit().toString());
                }
                //
                j.writeEndObject();
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
}

package com.intrbiz.bergamot.ui.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.reading.CheckReadingMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.lamplighter.data.LamplighterDB;
import com.intrbiz.lamplighter.model.CheckReading;
import com.intrbiz.lamplighter.model.StoredDoubleGaugeReading;
import com.intrbiz.lamplighter.model.StoredFloatGaugeReading;
import com.intrbiz.lamplighter.model.StoredIntGaugeReading;
import com.intrbiz.lamplighter.model.StoredLongGaugeReading;
import com.intrbiz.lamplighter.model.StoredMeterReading;
import com.intrbiz.lamplighter.model.StoredReading;
import com.intrbiz.lamplighter.model.StoredTimerReading;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckRegEx;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.IsaInt;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;

@Title("Lamplighter (Readings) API Methods")
@Desc({
    "Lamplighter is Bergamot Monitorings internal readings (metrics) sub-system.  Lamplighter collects readings (performance metrics published by checks) and stores them for later trend analysis.",
    "Lamplighter stores various types of metrics:",
    " * Gauges",
    " * * Int Gauge (32bit integer)",
    " * * Long Gauge (64bit iInteger)",
    " * * Float Gauge (32bit floating point",
    " * * Double Gauge (64bit floating point",
})
@Prefix("/api/lamplighter")
@RequireValidPrincipal()
public class LamplighterAPIRouter extends Router<BergamotApp>
{        
    @Title("Get readings for check")
    @Desc({
        "Get the list of available readings for the check identified by the given UUID.",
        "This will return metadata about all readings which are stored for a check, including reading ID, reading type."
    })
    @Any("/check/id/:id/readings")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @ListOf(CheckReadingMO.class)
    public List<CheckReadingMO> getReadingsByCheck(LamplighterDB db, @Var("site") Site site, @IsaObjectId() UUID id)
    {
        List<CheckReadingMO> readings = new LinkedList<CheckReadingMO>();
        for (CheckReading reading : db.getCheckReadingsForCheck(id))
        {
            readings.add(new CheckReadingMO(
                    reading.getId(),
                    reading.getSiteId(),
                    reading.getCheckId(),
                    reading.getName(),
                    reading.getSummary(),
                    reading.getDescription(),
                    reading.getUnit(),
                    reading.getReadingType(),
                    reading.getCreated() == null ? 0 : reading.getCreated().getTime(),
                    reading.getUpdated() == null ? 0 : reading.getUpdated().getTime(),
                    reading.getPollInterval()
            ));
        }
        return readings;
    }
    
    // double gauge
    
    @Title("Latest double gauge readings")
    @Desc({
        "Get the latest readings for a double gauge."
    })
    @Any("/graph/reading/gauge/double/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getLatestDoubleReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) Integer limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredDoubleGaugeReading> readings = db.getLatestDoubleGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, DOUBLE_GAUGE_SERIES);
    }
    
    @Title("Get double gauge readings")
    @Desc({
        "Get double gauge readings for the given period (from start to end) applying the given aggregation method over the given rollup period.",
        "For example we can get the 5 minute average using the `avg` aggregation method with rollup period of `300000`."
    })
    @Any("/graph/reading/gauge/double/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getDoubleReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) Long rollup, 
            @CheckRegEx(value="(avg|sum|min|max)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
            @IsaLong() Long start,
            @IsaLong() Long end,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredDoubleGaugeReading> readings = db.getDoubleGaugeReadingsByDate(checkReading.getSiteId(), checkReading.getId(), new Timestamp(start), new Timestamp(end), rollup, agg);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, DOUBLE_GAUGE_SERIES);
    }
    
    // float gauge
    
    @Title("Latest float gauge readings")
    @Desc({
        "Get the latest readings for a float gauge."
    })
    @Any("/graph/reading/gauge/float/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getLatestFloatReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) Integer limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredFloatGaugeReading> readings = db.getLatestFloatGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, FLOAT_GAUGE_SERIES);
    }
    
    @Title("Get float gauge readings")
    @Desc({
        "Get float gauge readings for the given period (from start to end) applying the given aggregation method over the given rollup period.",
        "For example we can get the 5 minute average using the `avg` aggregation method with rollup period of `300000`."
    })
    @Any("/graph/reading/gauge/float/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getFloatReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) Long rollup, 
            @CheckRegEx(value="(avg|sum|min|max)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
            @IsaLong() Long start,
            @IsaLong() Long end,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredFloatGaugeReading> readings = db.getFloatGaugeReadingsByDate(checkReading.getSiteId(), checkReading.getId(), new Timestamp(start), new Timestamp(end), rollup, agg);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, FLOAT_GAUGE_SERIES);
    }
    
    // long gauge
    
    @Title("Latest long gauge readings")
    @Desc({
        "Get the latest readings for a long gauge."
    })
    @Any("/graph/reading/gauge/long/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getLatestLongReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) Integer limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredLongGaugeReading> readings = db.getLatestLongGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, LONG_GAUGE_SERIES);
    }
    
    @Title("Get long gauge readings")
    @Desc({
        "Get long gauge readings for the given period (from start to end) applying the given aggregation method over the given rollup period.",
        "For example we can get the 5 minute average using the `avg` aggregation method with rollup period of `300000`."
    })
    @Any("/graph/reading/gauge/long/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getLongReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) Long rollup, 
            @CheckRegEx(value="(avg|sum|min|max)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
            @IsaLong() Long start,
            @IsaLong() Long end,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredLongGaugeReading> readings = db.getLongGaugeReadingsByDate(checkReading.getSiteId(), checkReading.getId(), new Timestamp(start), new Timestamp(end), rollup, agg);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, LONG_GAUGE_SERIES);
    }
    
    // int gauge
    
    @Title("Latest int gauge readings")
    @Desc({
        "Get the latest readings for a int gauge."
    })
    @Any("/graph/reading/gauge/int/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getLatestIntReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) Integer limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredIntGaugeReading> readings = db.getLatestIntGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, INT_GAUGE_SERIES);
    }
    
    @Title("Get int gauge readings")
    @Desc({
        "Get int gauge readings for the given period (from start to end) applying the given aggregation method over the given rollup period.",
        "For example we can get the 5 minute average using the `avg` aggregation method with rollup period of `300000`."
    })
    @Any("/graph/reading/gauge/int/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getIntReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) Long rollup, 
            @CheckRegEx(value="(avg|sum|min|max)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
            @IsaLong() Long start,
            @IsaLong() Long end,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredIntGaugeReading> readings = db.getIntGaugeReadingsByDate(checkReading.getSiteId(), checkReading.getId(), new Timestamp(start), new Timestamp(end), rollup, agg);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, INT_GAUGE_SERIES);
    }
    
    // meter
    
    @Title("Latest meter readings")
    @Desc({
        "Get the latest readings for a meter."
    })
    @Any("/graph/reading/meter/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getMeterReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) Integer limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredMeterReading> readings = db.getLatestMeterReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, METER_SERIES);
    }
    
    @Title("Get meter readings")
    @Desc({
        "Get meter readings for the given period (from start to end) applying the given aggregation method over the given rollup period.",
        "For example we can get the 5 minute average using the `avg` aggregation method with rollup period of `300000`."
    })
    @Any("/graph/reading/meter/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getMeterReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) Long rollup, 
            @CheckRegEx(value="(avg|sum|min|max)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
            @IsaLong() Long start,
            @IsaLong() Long end,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredMeterReading> readings = db.getMeterReadingsByDate(checkReading.getSiteId(), checkReading.getId(), new Timestamp(start), new Timestamp(end), rollup, agg);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, METER_SERIES);
    }
    
    // timer
    
    @Title("Latest timer readings")
    @Desc({
        "Get the latest readings for a timer."
    })
    @Any("/graph/reading/timer/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getTimerReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) Integer limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredTimerReading> readings = db.getLatestTimerReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, TIMER_SERIES);
    }
    
    @Title("Get timer readings")
    @Desc({
        "Get timer readings for the given period (from start to end) applying the given aggregation method over the given rollup period.",
        "For example we can get the 5 minute average using the `avg` aggregation method with rollup period of `300000`."
    })
    @Any("/graph/reading/timer/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    @IgnoreBinding(ignoreDocs = false)
    public void getTimerReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId() UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) Long rollup, 
            @CheckRegEx(value="(avg|sum|min|max)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
            @IsaLong() Long start,
            @IsaLong() Long end,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredTimerReading> readings = db.getTimerReadingsByDate(checkReading.getSiteId(), checkReading.getId(), new Timestamp(start), new Timestamp(end), rollup, agg);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, TIMER_SERIES);
    }
    
    // data output methods
    
    private <T extends StoredReading> void writeLineChartData(JsonGenerator jenny, CheckReading checkReading, List<T> readings, String requestedSeries, Series<T>[] series) throws IOException
    {
        jenny.writeStartObject();
        // title
        jenny.writeFieldName("title");
        jenny.writeString(checkReading.getSummary() + (Util.isEmpty(checkReading.getUnit()) ? "" : " (" + checkReading.getUnit() + ")"));
        // x-title
        jenny.writeFieldName("x-title");
        jenny.writeString("");
        // y-title
        jenny.writeFieldName("y-title");
        jenny.writeString(Util.isEmpty(checkReading.getUnit()) ? "" : checkReading.getUnit());
        // x
        jenny.writeFieldName("x");
        jenny.writeStartArray();
        for (T reading : readings)
        {
            jenny.writeObject(series[0].accessor.apply(reading));
        }
        jenny.writeEndArray();
        // y sets
        jenny.writeFieldName("y");
        jenny.writeStartArray();
        // value
        jenny.writeStartObject();
        jenny.writeFieldName("title");
        jenny.writeString(series[0].title);
        jenny.writeFieldName("colour");
        jenny.writeString(series[0].colour);
        jenny.writeFieldName("y");
        jenny.writeStartArray();
        for (T reading : readings)
        {
            jenny.writeObject(series[0].accessor.apply(reading));
        }
        jenny.writeEndArray();
        jenny.writeEndObject();
        // optional series
        if (! (Util.isEmpty(requestedSeries) || "none".equals(requestedSeries)))
        {
            for (int i = 1; i < series.length; i++)
            {
                Series<T> optional = series[i];
                if (requestedSeries.contains(optional.name))
                {
                    jenny.writeStartObject();
                    jenny.writeFieldName("title");
                    jenny.writeString(optional.title);
                    jenny.writeFieldName("colour");
                    jenny.writeString(optional.colour);
                    jenny.writeFieldName("y");
                    jenny.writeStartArray();
                    for (T reading : readings)
                    {
                        jenny.writeObject(optional.accessor.apply(reading));
                    }
                    jenny.writeEndArray();
                    jenny.writeEndObject();
                }
            }
        }
        // end y sets
        jenny.writeEndArray();
        jenny.writeEndObject();
    }
    
    private static class Series<T extends StoredReading>
    {
        public final String name;
        
        public final String title;
        
        public final String colour;
        
        public final Function<T, Number> accessor;
        
        public Series(String name, String title, String colour, Function<T, Number> accessor)
        {
            this.name = name;
            this.title = title;
            this.colour = colour;
            this.accessor = accessor;
        }
        
        @SafeVarargs
        public static <X extends StoredReading> Series<X>[] of(Series<X>... xs)
        {
            return xs;
        }
    }
    
    private static final Series<StoredDoubleGaugeReading>[] DOUBLE_GAUGE_SERIES = Series.of(
            new Series<StoredDoubleGaugeReading>("value", "Value", "#00BF00", (r) -> r.getValue()),
            new Series<StoredDoubleGaugeReading>("warning", "Warning", "#FFBF00", (r) -> r.getWarning()),
            new Series<StoredDoubleGaugeReading>("critical", "Critical", "#E20800", (r) -> r.getCritical()),
            new Series<StoredDoubleGaugeReading>("min", "Min", "#bbb0c4", (r) -> r.getMin()),
            new Series<StoredDoubleGaugeReading>("max", "Max", "#684a96", (r) -> r.getMax())
    );
    
    private static final Series<StoredFloatGaugeReading>[] FLOAT_GAUGE_SERIES = Series.of(
            new Series<StoredFloatGaugeReading>("value", "Value", "#00BF00", (r) -> r.getValue()),
            new Series<StoredFloatGaugeReading>("warning", "Warning", "#FFBF00", (r) -> r.getWarning()),
            new Series<StoredFloatGaugeReading>("critical", "Critical", "#E20800", (r) -> r.getCritical()),
            new Series<StoredFloatGaugeReading>("min", "Min", "#bbb0c4", (r) -> r.getMin()),
            new Series<StoredFloatGaugeReading>("max", "Max", "#684a96", (r) -> r.getMax())
    );
    
    private static final Series<StoredLongGaugeReading>[] LONG_GAUGE_SERIES = Series.of(
            new Series<StoredLongGaugeReading>("value", "Value", "#00BF00", (r) -> r.getValue()),
            new Series<StoredLongGaugeReading>("warning", "Warning", "#FFBF00", (r) -> r.getWarning()),
            new Series<StoredLongGaugeReading>("critical", "Critical", "#E20800", (r) -> r.getCritical()),
            new Series<StoredLongGaugeReading>("min", "Min", "#bbb0c4", (r) -> r.getMin()),
            new Series<StoredLongGaugeReading>("max", "Max", "#684a96", (r) -> r.getMax())
    );
    
    private static final Series<StoredIntGaugeReading>[] INT_GAUGE_SERIES = Series.of(
            new Series<StoredIntGaugeReading>("value", "Value", "#00BF00", (r) -> r.getValue()),
            new Series<StoredIntGaugeReading>("warning", "Warning", "#FFBF00", (r) -> r.getWarning()),
            new Series<StoredIntGaugeReading>("critical", "Critical", "#E20800", (r) -> r.getCritical()),
            new Series<StoredIntGaugeReading>("min", "Min", "#bbb0c4", (r) -> r.getMin()),
            new Series<StoredIntGaugeReading>("max", "Max", "#684a96", (r) -> r.getMax())
    );
    
    private static final Series<StoredMeterReading>[] METER_SERIES = Series.of(
            new Series<StoredMeterReading>("mean_rate", "Mean Rate", "#f3c362", (r) -> r.getMeanRate()),
            new Series<StoredMeterReading>("1_minute_rate", "1 Minute Rate", "#ffee97", (r) -> r.getOneMinuteRate()),
            new Series<StoredMeterReading>("5_minute_rate", "5 Minute Rate", "#9cea00", (r) -> r.getFiveMinuteRate()),
            new Series<StoredMeterReading>("15_minute_rate", "15 Minute Rate", "#ff557f", (r) -> r.getFifteenMinuteRate()),
            new Series<StoredMeterReading>("count", "Count", "#00BF00", (r) -> r.getCount())
    );
        
    private static final Series<StoredTimerReading>[] TIMER_SERIES = Series.of(
            new Series<StoredTimerReading>("mean", "Mean", "#fe8901", (r) -> r.getMean()),
            new Series<StoredTimerReading>("count", "Count", "#00BF00", (r) -> r.getCount()),
            new Series<StoredTimerReading>("mean_rate", "Mean Rate", "#f3c362", (r) -> r.getMeanRate()),
            new Series<StoredTimerReading>("1_minute_rate", "1 Minute Rate", "#ffee97", (r) -> r.getOneMinuteRate()),
            new Series<StoredTimerReading>("5_minute_rate", "5 Minute Rate", "#9cea00", (r) -> r.getFiveMinuteRate()),
            new Series<StoredTimerReading>("15_minute_rate", "15 Minute Rate", "#ff557f", (r) -> r.getFifteenMinuteRate()),
            new Series<StoredTimerReading>("median", "Median", "#b88855", (r) -> r.getMedian()),
            new Series<StoredTimerReading>("min", "Min", "#bbb0c4", (r) -> r.getMin()),
            new Series<StoredTimerReading>("max", "Max", "#684a96", (r) -> r.getMax()),
            new Series<StoredTimerReading>("std_dev", "Std. Dev.", "#d13477", (r) -> r.getStdDev()),
            new Series<StoredTimerReading>("75th", "75%", "#009a49", (r) -> r.getThe75thPercentile()),
            new Series<StoredTimerReading>("95th", "95%", "#a6c67d", (r) -> r.getThe95thPercentile()),
            new Series<StoredTimerReading>("98th", "98%", "#79c0b5", (r) -> r.getThe98thPercentile()),
            new Series<StoredTimerReading>("99th", "99%", "#59aec3", (r) -> r.getThe99thPercentile()),
            new Series<StoredTimerReading>("999th", "99.9%", "#4d68dc", (r) -> r.getThe999thPercentile())
            
            
    );
}

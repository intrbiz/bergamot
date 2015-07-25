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
import com.intrbiz.lamplighter.model.StoredReading;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckRegEx;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.IsaInt;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;

@Prefix("/api/lamplighter")
@RequireValidPrincipal()
public class LamplighterAPIRouter extends Router<BergamotApp>
{        
    @Any("/check/id/:id/readings")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public List<CheckReadingMO> getReadingsByCheck(LamplighterDB db, @Var("site") Site site, @IsaObjectId(session = false) UUID id)
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
    
    @Any("/graph/reading/gauge/double/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getLatestDoubleReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) int limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredDoubleGaugeReading> readings = db.getLatestDoubleGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, StoredDoubleGaugeReading::getValue, StoredDoubleGaugeReading::getWarning, StoredDoubleGaugeReading::getCritical, StoredDoubleGaugeReading::getMin, StoredDoubleGaugeReading::getMax);
    }
    
    @Any("/graph/reading/gauge/double/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getDoubleReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) long rollup, 
            @CheckRegEx(value="(avg|sum)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
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
        this.writeLineChartData(jenny, checkReading, readings, series, StoredDoubleGaugeReading::getValue, StoredDoubleGaugeReading::getWarning, StoredDoubleGaugeReading::getCritical, StoredDoubleGaugeReading::getMin, StoredDoubleGaugeReading::getMax);
    }
    
    // float gauge
    
    @Any("/graph/reading/gauge/float/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getLatestFloatReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) int limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredFloatGaugeReading> readings = db.getLatestFloatGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, StoredFloatGaugeReading::getValue, StoredFloatGaugeReading::getWarning, StoredFloatGaugeReading::getCritical, StoredFloatGaugeReading::getMin, StoredFloatGaugeReading::getMax);
    }
    
    @Any("/graph/reading/gauge/float/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getFloatReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) long rollup, 
            @CheckRegEx(value="(avg|sum)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
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
        this.writeLineChartData(jenny, checkReading, readings, series, StoredFloatGaugeReading::getValue, StoredFloatGaugeReading::getWarning, StoredFloatGaugeReading::getCritical, StoredFloatGaugeReading::getMin, StoredFloatGaugeReading::getMax);
    }
    
    // long gauge
    
    @Any("/graph/reading/gauge/long/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getLatestLongReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) int limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredLongGaugeReading> readings = db.getLatestLongGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, StoredLongGaugeReading::getValue, StoredLongGaugeReading::getWarning, StoredLongGaugeReading::getCritical, StoredLongGaugeReading::getMin, StoredLongGaugeReading::getMax);
    }
    
    @Any("/graph/reading/gauge/long/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getLongReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) long rollup, 
            @CheckRegEx(value="(avg|sum)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
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
        this.writeLineChartData(jenny, checkReading, readings, series, StoredLongGaugeReading::getValue, StoredLongGaugeReading::getWarning, StoredLongGaugeReading::getCritical, StoredLongGaugeReading::getMin, StoredLongGaugeReading::getMax);
    }
    
    // int gauge
    
    @Any("/graph/reading/gauge/int/:id/latest/:limit")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getLatestIntReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) int limit,
            @Param("series") String series
    ) throws IOException
    {
        // get the data
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredIntGaugeReading> readings = db.getLatestIntGaugeReadings(site.getId(), id, limit);
        // write
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        this.writeLineChartData(jenny, checkReading, readings, series, StoredIntGaugeReading::getValue, StoredIntGaugeReading::getWarning, StoredIntGaugeReading::getCritical, StoredIntGaugeReading::getMin, StoredIntGaugeReading::getMax);
    }
    
    @Any("/graph/reading/gauge/int/:id/date/:rollup/:agg/:start/:end")
    @RequirePermission("api.read.lamplighter.readings")
    @WithDataAdapter(LamplighterDB.class)
    public void getIntReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaLong(mandatory = true, defaultValue = 300_000L, coalesce = CoalesceMode.ALWAYS) long rollup, 
            @CheckRegEx(value="(avg|sum)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
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
        this.writeLineChartData(jenny, checkReading, readings, series, StoredIntGaugeReading::getValue, StoredIntGaugeReading::getWarning, StoredIntGaugeReading::getCritical, StoredIntGaugeReading::getMin, StoredIntGaugeReading::getMax);
    }
    
    // generic
    
    /**
     * Generically output a line chart JSON data structure
     * @param jenny the json output stream
     * @param checkReading the check reading
     * @param readings the data points
     * @param series which series to output
     * @param getValue the value accessor
     * @param getWarning the warning accessor
     * @param getCritical the critical accessor
     * @param getMin the min accessor
     * @param getMax the max accessor
     * @throws IOException
     */
    private <T extends StoredReading> void writeLineChartData(JsonGenerator jenny, CheckReading checkReading, List<T> readings, String series, Function<T,Object> getValue, Function<T,Object> getWarning, Function<T,Object> getCritical, Function<T,Object> getMin, Function<T,Object> getMax) throws IOException
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
            jenny.writeNumber(reading.getCollectedAt().getTime());
        }
        jenny.writeEndArray();
        // y sets
        jenny.writeFieldName("y");
        jenny.writeStartArray();
        // value
        jenny.writeStartObject();
        jenny.writeFieldName("title");
        jenny.writeString(checkReading.getSummary() + (Util.isEmpty(checkReading.getUnit()) ? "" : " (" + checkReading.getUnit() + ")"));
        jenny.writeFieldName("colour");
        jenny.writeString("#00BF00");
        jenny.writeFieldName("y");
        jenny.writeStartArray();
        for (T reading : readings)
        {
            jenny.writeObject(getValue.apply(reading));
        }
        jenny.writeEndArray();
        jenny.writeEndObject();
        // optional series
        if (! (Util.isEmpty(series) || "none".equals(series)))
        {
            // warning
            if (series.contains("warning"))
            {
                jenny.writeStartObject();
                jenny.writeFieldName("title");
                jenny.writeString("Warning");
                jenny.writeFieldName("colour");
                jenny.writeString("#FFBF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (T reading : readings)
                {
                    jenny.writeObject(getWarning.apply(reading));
                }
                jenny.writeEndArray();
                jenny.writeEndObject();
            }
            // critical
            if (series.contains("critical"))
            {
                jenny.writeStartObject();
                jenny.writeFieldName("title");
                jenny.writeString("Critical");
                jenny.writeFieldName("colour");
                jenny.writeString("#E20800");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (T reading : readings)
                {
                    jenny.writeObject(getCritical.apply(reading));
                }
                jenny.writeEndArray();
                jenny.writeEndObject();
            }
            // min
            if (series.contains("min"))
            {
                jenny.writeStartObject();
                jenny.writeFieldName("title");
                jenny.writeString("Min");
                jenny.writeFieldName("colour");
                jenny.writeString("#A4C0E4");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (T reading : readings)
                {
                    jenny.writeObject(getMin.apply(reading));
                }
                jenny.writeEndArray();
                jenny.writeEndObject();
            }
            // max
            if (series.contains("max"))
            {
                jenny.writeStartObject();
                jenny.writeFieldName("title");
                jenny.writeString("Max");
                jenny.writeFieldName("colour");
                jenny.writeString("#A4C0E4");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (T reading : readings)
                {
                    jenny.writeObject(getMax.apply(reading));
                }
                jenny.writeEndArray();
                jenny.writeEndObject();
            }
        }
        // end y sets
        jenny.writeEndArray();
        jenny.writeEndObject();
    }
}

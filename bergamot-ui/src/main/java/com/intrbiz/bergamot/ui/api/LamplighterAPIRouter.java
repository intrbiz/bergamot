package com.intrbiz.bergamot.ui.api;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckRegEx;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.IsaInt;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;

@Prefix("/api/lamplighter")
@RequireValidPrincipal()
public class LamplighterAPIRouter extends Router<BergamotApp>
{        
    @Any("/check/id/:id/readings")
    @JSON(notFoundIfNull = true)
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
                    reading.getUpdated() == null ? 0 : reading.getUpdated().getTime()
            ));
        }
        return readings;
    }
    
    @Any("/graph/reading/gauge/double/:id/latest/:limit")
    @WithDataAdapter(LamplighterDB.class)
    public void getLatestDoubleReadings(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @IsaInt(min = 1, max = 1000, defaultValue = 100, coalesce = CoalesceMode.ALWAYS) int limit,
            @Param("series") String series
    ) throws IOException
    {
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredDoubleGaugeReading> readings = db.getLatestDoubleGaugeReadings(site.getId(), id, limit);
        //
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        jenny.writeStartObject();
        // title
        jenny.writeFieldName("title");
        jenny.writeString(checkReading.getSummary());
        // x-title
        jenny.writeFieldName("x-title");
        jenny.writeString("");
        // y-title
        jenny.writeFieldName("y-title");
        jenny.writeString("");
        // x
        jenny.writeFieldName("x");
        jenny.writeStartArray();
        for (StoredDoubleGaugeReading reading : readings)
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
        jenny.writeString("#FF0000");
        jenny.writeFieldName("y");
        jenny.writeStartArray();
        for (StoredDoubleGaugeReading reading : readings)
        {
            jenny.writeNumber(reading.getValue());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getWarning());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getCritical());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getMin());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getMax());
                }
                jenny.writeEndArray();
                jenny.writeEndObject();
            }
        }
        // end y sets
        jenny.writeEndArray();
        jenny.writeEndObject();
    }
    
    @Any("/graph/reading/gauge/double/:id/date/:rollup/:agg/:start/:end")
    @WithDataAdapter(LamplighterDB.class)
    public void getDoubleReadingsByDate(
            LamplighterDB db, 
            @Var("site") Site site, 
            @IsaObjectId(session = false) UUID id, 
            @CheckRegEx(value="(minute|hour|day|month)", mandatory = true, defaultValue = "hour", coalesce = CoalesceMode.ALWAYS) String rollup, 
            @CheckRegEx(value="(avg|sum)", mandatory = true, defaultValue = "avg", coalesce = CoalesceMode.ALWAYS) String agg,
            @IsaLong() Long start,
            @IsaLong() Long end,
            @Param("series") String series
    ) throws IOException
    {
        CheckReading checkReading = db.getCheckReading(id);
        List<StoredDoubleGaugeReading> readings = db.getDoubleGaugeReadingsByDate(checkReading.getSiteId(), checkReading.getId(), new Timestamp(start), new Timestamp(end), rollup, agg);
        //
        JsonGenerator jenny = response().ok().json().getJsonWriter();
        jenny.writeStartObject();
        // title
        jenny.writeFieldName("title");
        jenny.writeString(checkReading.getSummary());
        // x-title
        jenny.writeFieldName("x-title");
        jenny.writeString("");
        // y-title
        jenny.writeFieldName("y-title");
        jenny.writeString("");
        // x
        jenny.writeFieldName("x");
        jenny.writeStartArray();
        for (StoredDoubleGaugeReading reading : readings)
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
        jenny.writeString("#FF0000");
        jenny.writeFieldName("y");
        jenny.writeStartArray();
        for (StoredDoubleGaugeReading reading : readings)
        {
            jenny.writeNumber(reading.getValue());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getWarning());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getCritical());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getMin());
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
                jenny.writeString("#00FF00");
                jenny.writeFieldName("y");
                jenny.writeStartArray();
                for (StoredDoubleGaugeReading reading : readings)
                {
                    jenny.writeNumber(reading.getMax());
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
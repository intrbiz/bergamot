package com.intrbiz.bergamot.ui.api;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Downtime;
import com.intrbiz.bergamot.model.message.DowntimeMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;

@Prefix("/api/downtime")
@RequireValidPrincipal()
public class DowntimeAPIRouter extends Router<BergamotApp>
{

    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public DowntimeMO getComment(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getDowntime(id), Downtime::toMO);
    }
    
    @Get("/id/:id/remove")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public Boolean removeDowntime(BergamotDB db, @AsUUID UUID id)
    {
        db.removeDowntime(id);
        return true;
    }
    
    @Get("/for-object/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public List<DowntimeMO> getDowntimeForObject(
            BergamotDB db, 
            @AsUUID UUID id, 
            @Param("past") @IsaLong(min = 0, max = 365, mandatory = true, defaultValue = 7, coalesce = CoalesceMode.ON_NULL) Integer pastDays, 
            @Param("future") @IsaLong(min = 0, max = 365, mandatory = true, defaultValue = 7, coalesce = CoalesceMode.ON_NULL) Integer futureDays
    )
    {
        return db.getDowntimesForCheck(id, pastDays + " days", futureDays + " days").stream().map(Downtime::toMO).collect(Collectors.toList());
    }
    
    @Any("/add-downtime-to-check/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public DowntimeMO addDowntimeToCheck(
            BergamotDB db, 
            @AsUUID UUID id,
            @Param("starts") @IsaLong(min = 0, mandatory = true) Long startTime,
            @Param("ends") @IsaLong(min = 0, mandatory = true) Long endTime,
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary,
            @Param("description") @CheckStringLength(min = 1, max = 4096, mandatory = true) String description
    )
    {
        Check<?, ?> check = db.getCheck(id);
        if (check == null) throw new BalsaNotFound("No check with id: " + id);
        //
        Timestamp starts = new Timestamp(startTime);
        Timestamp ends = new Timestamp(endTime);
        //
        Downtime downtime = new Downtime().createdBy(this.currentPrincipal()).on(check).between(starts, ends).summary(summary).description(description);
        db.setDowntime(downtime);
        return downtime.toMO();
    }
}

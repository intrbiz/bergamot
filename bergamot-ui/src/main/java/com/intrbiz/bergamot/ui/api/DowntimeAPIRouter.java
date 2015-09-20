package com.intrbiz.bergamot.ui.api;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Downtime;
import com.intrbiz.bergamot.model.message.DowntimeMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsDate;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaInt;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
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
    public DowntimeMO getDowntime(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Downtime downtime = notNull(db.getDowntime(id));
        require(permission("read.downtime", downtime.getCheckId()));
        return downtime.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/remove")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public Boolean removeDowntime(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Downtime downtime = db.getDowntime(id);
        if (downtime != null)
        {
            require(permission("remove.downtime", downtime.getCheckId()));
            db.removeDowntime(id);
        }
        return true;
    }
    
    @Get("/for-object/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    @ListOf(DowntimeMO.class)
    public List<DowntimeMO> getDowntimeForObject(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("past") @IsaInt(min = 0, max = 365, mandatory = true, defaultValue = 7, coalesce = CoalesceMode.ON_NULL) Integer pastDays, 
            @Param("future") @IsaInt(min = 0, max = 365, mandatory = true, defaultValue = 7, coalesce = CoalesceMode.ON_NULL) Integer futureDays
    )
    {
        require(permission("read.downtime", id));
        return db.getDowntimesForCheck(id, pastDays + " days", futureDays + " days").stream().map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Any("/add-downtime-to-check/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public DowntimeMO addDowntimeToCheck(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id,
            @Param("starts") @AsDate("yyyy-MM-dd HH:mm") Date startTime,
            @Param("ends") @AsDate("yyyy-MM-dd HH:mm") Date endTime,
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary,
            @Param("description") @CheckStringLength(min = 1, max = 4096, mandatory = true) String description
    )
    {
        // TODO: timezone handling
        Check<?, ?> check = notNull(db.getCheck(id));
        require(permission("write.downtime", check));
        //
        Timestamp starts = new Timestamp(startTime.getTime());
        Timestamp ends = new Timestamp(endTime.getTime());
        //
        Downtime downtime = new Downtime().createdBy(this.currentPrincipal()).on(check).between(starts, ends).summary(summary).description(description);
        db.setDowntime(downtime);
        return downtime.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/render")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public String renderDowntime(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Downtime downtime = var("downtime", notNull(db.getDowntime(id)));
        require(permission("read.downtime", downtime.getCheckId()));
        return encodeBuffered("include/downtime");
    }
}

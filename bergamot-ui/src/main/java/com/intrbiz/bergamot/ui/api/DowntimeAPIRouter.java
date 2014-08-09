package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Downtime;
import com.intrbiz.bergamot.model.message.DowntimeMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
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
}

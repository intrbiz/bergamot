package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Var;


@Prefix("/api/time-period")
public class TimePeriodAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<TimePeriodMO> getTimePeriods(BergamotDB db, @Var("site") Site site)
    {
        return db.listTimePeriods(site.getId()).stream().map(TimePeriod::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO getTimePeriod(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTimePeriodByName(site.getId(), name), TimePeriod::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO getTimePeriod(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTimePeriod(id), TimePeriod::toMO);
    }
}

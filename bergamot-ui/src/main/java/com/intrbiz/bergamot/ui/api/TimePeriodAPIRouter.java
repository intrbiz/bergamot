package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/time-period")
@RequireValidPrincipal()
public class TimePeriodAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @RequirePermission("api.read.time-period")
    @WithDataAdapter(BergamotDB.class)
    public List<TimePeriodMO> getTimePeriods(BergamotDB db, @Var("site") Site site)
    {
        return db.listTimePeriods(site.getId()).stream().map(TimePeriod::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.time-period")
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO getTimePeriod(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTimePeriodByName(site.getId(), name), TimePeriod::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.time-period")
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO getTimePeriod(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getTimePeriod(id), TimePeriod::toMO);
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.time-period.config")
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodCfg getTimePeriodConfig(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTimePeriodByName(site.getId(), name), TimePeriod::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.time-period.config")
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodCfg getTimePeriodConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getTimePeriod(id), TimePeriod::getConfiguration);
    }
}

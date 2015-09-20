package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/time-period")
@RequireValidPrincipal()
public class TimePeriodAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(TimePeriodMO.class)
    public List<TimePeriodMO> getTimePeriods(BergamotDB db, @Var("site") Site site)
    {
        return db.listTimePeriods(site.getId()).stream().filter((x) -> permission("read", x)).map((x) -> x.toStubMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO getTimePeriodByName(BergamotDB db, @Var("site") Site site, String name)
    {
        TimePeriod timePeriod = notNull(db.getTimePeriodByName(site.getId(), name));
        require(permission("read", timePeriod));
        return timePeriod.toMO(currentPrincipal());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO getTimePeriod(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        TimePeriod timePeriod = notNull(db.getTimePeriod(id));
        require(permission("read", timePeriod));
        return timePeriod.toMO(currentPrincipal());
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public TimePeriodCfg getTimePeriodConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        TimePeriod timePeriod = notNull(db.getTimePeriodByName(site.getId(), name));
        require(permission("read.config", timePeriod));
        return timePeriod.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public TimePeriodCfg getTimePeriodConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        TimePeriod timePeriod = notNull(db.getTimePeriod(id));
        require(permission("read", timePeriod));
        return timePeriod.getConfiguration();
    }
}

package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.timerange.TimeRangeParser;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsBoolean;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Param;
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
    public TimePeriodMO getTimePeriod(BergamotDB db, @AsUUID UUID id)
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
    public TimePeriodCfg getTimePeriodConfig(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTimePeriod(id), TimePeriod::getConfiguration);
    }
    
    @Any("/configure")
    @JSON()
    @RequirePermission("api.write.time-period.create")
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO configureTimePeriod(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("configuration") @CheckStringLength(min = 1, max = 128 * 1024, mandatory = true) String configurationXML
    )
    {
        // parse the config and allocate the id
        TimePeriodCfg config = TimePeriodCfg.fromString(TimePeriodCfg.class, configurationXML);
        config.setId(site.randomObjectId());
        // create the time period
        TimePeriod timePeriod = action("create-time-period", config);
        return timePeriod.toMO();
    }
    
    @Any("/create")
    @JSON()
    @RequirePermission("api.write.time-period.create")
    @WithDataAdapter(BergamotDB.class)
    public TimePeriodMO createTimePeriod(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("name") @CheckStringLength(min = 1, max = 80, mandatory = true) String name, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("description") @CheckStringLength(min = 1, max = 1000) String description, 
            @Param("template") @AsBoolean(coalesce = CoalesceMode.ON_NULL) Boolean template, 
            @ListParam("extends") @CheckStringLength(min = 1, max = 80, mandatory = true) List<String> inherits, 
            @ListParam("exclude") @CheckStringLength(min = 1, max = 80, mandatory = true) List<String> excludes, 
            @ListParam("time-range") @CheckStringLength(min = 1, max = 50, mandatory = true) List<String> timeRanges
    )
    {
        // parse the config and allocate the id
        TimePeriodCfg config = new TimePeriodCfg();
        config.setId(site.randomObjectId());
        config.setName(name);
        config.setSummary(summary);
        config.setDescription(description);
        config.setTemplate(template);
        for (String inherit : inherits)
        {
            config.getInheritedTemplates().add(inherit);
        }
        for (String exclude : excludes)
        {
            config.getExcludes().add(exclude);
        }
        for (String timeRange : timeRanges)
        {
            config.getTimeRanges().add(TimeRangeParser.parseTimeRange(timeRange));
        }
        // create the time period
        TimePeriod timePeriod = action("create-time-period", config);
        return timePeriod.toMO();
    }
}

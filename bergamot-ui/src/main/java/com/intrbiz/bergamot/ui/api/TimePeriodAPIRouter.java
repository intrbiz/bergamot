package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/time-period")
public class TimePeriodAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<TimePeriodMO> getTimePeriods()
    {
        return null; //return this.app().getBergamot().getObjectStore().getTimePeriods().stream().map(TimePeriod::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public TimePeriodMO getTimePeriod(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupTimePeriod(name), TimePeriod::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public TimePeriodMO getTimePeriod(@AsUUID UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupTimePeriod(id), TimePeriod::toMO);
    }
}

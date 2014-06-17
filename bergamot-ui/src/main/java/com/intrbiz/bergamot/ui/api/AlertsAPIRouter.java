package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Var;


@Prefix("/api/alert")
public class AlertsAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<CheckMO> getAlerts(BergamotDB db, @Var("site") Site site)
    {
        return db.listChecksThatAreNotOk(site.getId()).stream().map((c) -> {return (CheckMO) c.toMO();}).collect(Collectors.toList());
    }
}

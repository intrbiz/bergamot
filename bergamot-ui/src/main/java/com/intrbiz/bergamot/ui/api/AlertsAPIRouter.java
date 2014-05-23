package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/alert")
public class AlertsAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<CheckMO> getAlerts()
    {
        return this.app().getBergamot().getObjectStore().getAlerts().stream().map(Check::toStubMO).collect(Collectors.toList());
    }
}

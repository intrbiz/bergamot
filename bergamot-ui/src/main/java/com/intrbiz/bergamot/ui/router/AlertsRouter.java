package com.intrbiz.bergamot.ui.router;

import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/alerts")
@Template("layout/main")
@RequireValidPrincipal()
public class AlertsRouter extends Router<BergamotUI>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void activeAlerts(BergamotDB db, @GetBergamotSite() Site site, @CurrentPrincipal() Contact contact)
    {
        model("alerts", db.listAlertsForContact(site.getId(), contact.getId()).stream().map(Alert::getCheck).collect(Collectors.toList()));
        encode("alerts/index");
    }
    
    @Any("/history")
    @WithDataAdapter(BergamotDB.class)
    public void historicAlerts(
            BergamotDB db, 
            @GetBergamotSite() Site site,
            @CurrentPrincipal() Contact contact,
            @Param("offset") @IsaLong(min = 0, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 0L)  long offset,
            @Param("limit")  @IsaLong(min = 1, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 10L) long limit
    )
    {
        model("alerts", db.listAlertHistoryForContact(site.getId(), contact.getId(), offset, limit));
        var("offset", offset);
        var("limit", limit);
        encode("alerts/historic");
    }
}

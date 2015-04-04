package com.intrbiz.bergamot.ui.api;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.AlertMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;



@Prefix("/api/alert")
@RequireValidPrincipal()
public class AlertsAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<AlertMO> getAlerts(BergamotDB db, @Var("site") Site site)
    {
        return db.listAlerts(site.getId()).stream().map(Alert::toMO).collect(Collectors.toList());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO getAlert(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getAlert(id), Alert::toMO);
    }
    
    @Get("/for-check/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public List<AlertMO> getAlertsForCheck(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return db.getAlertsForCheck(id).stream().map(Alert::toMO).collect(Collectors.toList());
    }
    
    @Get("/current/for-check/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO getCurrentAlertForCheck(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCurrentAlertForCheck(id), Alert::toMO);
    }
    
    @Any("/id/:id/acknowledge")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO acknowledgeAlert(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id,
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 0, max = 4096, mandatory = false) String comment
    )
    {
        Alert alert = db.getAlert(id);
        if (alert == null) return null;
        // can only acknowledge an non-recovered and non-acknowledged alert
        if (! (alert.isAcknowledged() || alert.isRecovered()))
        {
            // the contact
            Contact contact = currentPrincipal();
            // acknowledge
            db.execute(() -> {
                Comment ackCom = new Comment().author(contact).acknowledges(alert).summary(summary).message(comment);
                db.setComment(ackCom);
                alert.setAcknowledged(true);
                alert.setAcknowledgedAt(new Timestamp(System.currentTimeMillis()));
                alert.setAcknowledgedById(contact.getId());
                db.setAlert(alert);
            });
        }
        return alert.toMO();
    }
    
    @Get("/id/:id/render")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public String renderAlert(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Alert alert = var("alert", db.getAlert(id));
        return alert == null ? null : encodeBuffered("include/alert");
    }
    
    @Get("/id/:id/dashboard/render")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String renderComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Alert alert = db.getAlert(id);
        if (alert == null) return null;
        var("check", alert.getCheck());
        var("alert", true);
        return encodeBuffered("include/check");
    }
}

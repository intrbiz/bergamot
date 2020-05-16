package com.intrbiz.bergamot.ui.api;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.Accounting;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.AlertMO;
import com.intrbiz.bergamot.model.message.event.update.AlertUpdate;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.result.AbstractResultProcessor;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;


@Title("Alert API Methods")
@Desc({
    "Alerts are raised when a check has reached a steady not OK state and someone or something needs to be notified."
})
@Prefix("/api/alert")
@RequireValidPrincipal()
public class AlertsAPIRouter extends Router<BergamotUI>
{
    private Logger logger = Logger.getLogger(AlertsAPIRouter.class);
    
    private Accounting accounting = Accounting.create(AlertsAPIRouter.class);
    
    public AlertsAPIRouter()
    {
    }
    
    @Title("List alerts")
    @Desc({
        "Get the list of all currently active alerts, returning minimal information about each alert."
    })
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(AlertMO.class)
    public List<AlertMO> getAlerts(BergamotDB db, @Var("site") Site site)
    {
        return db.listAlerts(site.getId()).stream().filter((a) -> permission("read", a.getCheckId())).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Title("Get alert")
    @Desc({
        "Get alert identified by the given UUID"
    })
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO getAlert(BergamotDB db, @IsaObjectId() UUID id)
    {
        Alert alert = notNull(db.getAlert(id));
        require(permission("read", alert.getCheckId()));
        return alert.toMO(currentPrincipal());
    }
    
    @Title("Get alerts for check")
    @Desc({
        "Get the alerts for the given check identified by the given UUID."
    })
    @Get("/for-check/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    @ListOf(AlertMO.class)
    public List<AlertMO> getAlertsForCheck(BergamotDB db, @IsaObjectId() UUID id)
    {
        require(permission("read", id));
        return db.getAlertsForCheck(id).stream().map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Title("Get current alert for check")
    @Desc({
        "Get the current alert for the given check identified by the given UUID."
    })
    @Get("/current/for-check/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO getCurrentAlertForCheck(BergamotDB db, @IsaObjectId() UUID id)
    {
        Alert alert = notNull(db.getCurrentAlertForCheck(id));
        require(permission("read", alert.getCheckId()));
        return alert.toMO(currentPrincipal());
    }
    
    @Title("Acknowledge alert")
    @Desc({
        "Acknowledge the alert identified by the given UUID with the given summary and comment"
    })
    @Any("/id/:id/acknowledge")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO acknowledgeAlert(
            BergamotDB db, 
            @IsaObjectId() UUID id,
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 0, max = 4096, mandatory = false) String comment
    )
    {
        Alert alert = notNull(db.getAlert(id));
        require(permission("acknowledge", alert.getCheckId()));
        // can only acknowledge an non-recovered and non-acknowledged alert
        if (! (alert.isAcknowledged() || alert.isRecovered()))
        {
            // the contact
            Contact contact = currentPrincipal();
            // the comment to add
            Comment ackCom = new Comment().author(contact).acknowledges(alert).summary(summary).message(comment);
            // acknowledge
            db.execute(() -> {
                db.setComment(ackCom);
                alert.setAcknowledged(true);
                alert.setAcknowledgedAt(new Timestamp(System.currentTimeMillis()));
                alert.setAcknowledgedById(contact.getId());
                db.setAlert(alert);
                db.acknowledgeCheck(alert.getCheckId(), true);
            });
            // send acknowledge notifications
            Check<?,?> check = alert.getCheck();
            CheckState state = check.getState();
            ((AbstractResultProcessor) app().getProcessor().getResultProcessor()).publishAcknowledgeNotification(check, state, alert, alert.getNotified(), Calendar.getInstance(), contact, ackCom);
            // send alert update
            action("publish-update", alert.getSiteId(), new AlertUpdate(alert.toMO(currentPrincipal())));
        }
        return alert.toMO(currentPrincipal());
    }
    
    @Title("False positive alert")
    @Desc({
        "Mark the alert identified by the given UUID as a false positive with the given summary and comment"
    })
    @Any("/id/:id/falsepositive")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO falsePositiveAlert(
            BergamotDB db, 
            @IsaObjectId() UUID id,
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("comment") @CheckStringLength(min = 0, max = 4096, mandatory = false) String comment
    )
    {
        Alert alert = notNull(db.getAlert(id));
        require(permission("acknowledge", alert.getCheckId()));
        // Can only mark an alert as a false positive if it has been acknowledged or recovered
        if ((! alert.isFalsePositive()) && (alert.isAcknowledged() || alert.isRecovered()))
        {
            // the contact
            Contact contact = currentPrincipal();
            // the comment to add
            Comment ackCom = new Comment().author(contact).falsePositive(alert).summary(summary).message(comment);
            // acknowledge
            db.execute(() -> {
                db.setComment(ackCom);
                alert.setFalsePositive(true);
                alert.setFalsePositiveAt(new Timestamp(System.currentTimeMillis()));
                alert.setFalsePositiveById(contact.getId());
                alert.setFalsePositiveReasonId(ackCom.getId());
                db.setAlert(alert);
            });
            // send alert update
            action("publish-update", alert.getSiteId(), new AlertUpdate(alert.toMO(currentPrincipal())));
        }
        return alert.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/render")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public String renderAlert(BergamotDB db, @IsaObjectId() UUID id)
    {
        Alert alert = var("alert", notNull(db.getAlert(id)));
        require(permission("read", alert.getCheckId()));
        return encodeBuffered("include/alert");
    }
    
    @Get("/id/:id/dashboard/render")
    @JSON()
    @RequirePermission("api.read.alert")
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public String renderComment(BergamotDB db, @IsaObjectId() UUID id)
    {
        Alert alert = notNull(db.getAlert(id));
        require(permission("read", alert.getCheckId()));
        var("check", alert.getCheck());
        var("alert", true);
        return encodeBuffered("include/check");
    }
}

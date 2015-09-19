package com.intrbiz.bergamot.ui.api;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.AlertMO;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.SendAcknowledge;
import com.intrbiz.bergamot.model.message.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.bergamot.queue.key.UpdateKey;
import com.intrbiz.bergamot.queue.key.UpdateKey.UpdateType;
import com.intrbiz.bergamot.ui.BergamotApp;
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
import com.intrbiz.queue.RoutedProducer;



@Prefix("/api/alert")
@RequireValidPrincipal()
public class AlertsAPIRouter extends Router<BergamotApp>
{
    private Logger logger = Logger.getLogger(AlertsAPIRouter.class);
    
    private NotificationQueue notificationQueue;
    
    private RoutedProducer<Notification, NotificationKey> notificationsProducer;
    
    private UpdateQueue updateQueue;
    
    private RoutedProducer<Update, UpdateKey> updateProducer;
    
    public AlertsAPIRouter()
    {
        // notifications
        this.notificationQueue = NotificationQueue.open();
        this.notificationsProducer = this.notificationQueue.publishNotifications();
        // updates
        this.updateQueue = UpdateQueue.open();
        this.updateProducer = this.updateQueue.publishUpdates();
    }
    
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(AlertMO.class)
    public List<AlertMO> getAlerts(BergamotDB db, @Var("site") Site site)
    {
        return db.listAlerts(site.getId()).stream().filter((a) -> permission("read", a.getCheckId())).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO getAlert(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Alert alert = notNull(db.getAlert(id));
        require(permission("read", alert.getCheckId()));
        return alert.toMO(currentPrincipal());
    }
    
    @Get("/for-check/id/:id")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    @ListOf(AlertMO.class)
    public List<AlertMO> getAlertsForCheck(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        require(permission("read", id));
        return db.getAlertsForCheck(id).stream().map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/current/for-check/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public AlertMO getCurrentAlertForCheck(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Alert alert = notNull(db.getCurrentAlertForCheck(id));
        require(permission("read", alert.getCheckId()));
        return alert.toMO(currentPrincipal());
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
            });
            // send acknowledge notifications
            if (! alert.getCheck().getState().isSuppressedOrInDowntime())
            {
                SendAcknowledge sendAck = alert.createAcknowledgeNotification(Calendar.getInstance(), contact, ackCom);
                if (sendAck != null && (! sendAck.getTo().isEmpty()))
                {
                    logger.warn("Sending acknowledge for " + alert.getId());
                    this.notificationsProducer.publish(new NotificationKey(contact.getSite().getId()), sendAck);
                }
                else
                {
                    logger.warn("Not sending acknowledge for " + alert.getId());
                }
            }
            // send alert update
            this.updateProducer.publish(new UpdateKey(UpdateType.ALERT, alert.getSiteId(), alert.getId()), new AlertUpdate(alert.toMO(currentPrincipal())));
        }
        return alert.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/render")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public String renderAlert(BergamotDB db, @IsaObjectId(session = false) UUID id)
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
    public String renderComment(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Alert alert = notNull(db.getAlert(id));
        require(permission("read", alert.getCheckId()));
        var("check", alert.getCheck());
        var("alert", true);
        return encodeBuffered("include/check");
    }
}

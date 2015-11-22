package com.intrbiz.bergamot.model;

import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.AlertEscalationMO;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "alert_escalation", since = @SQLVersion({ 3, 26, 0 }))
public class AlertEscalation extends BergamotObject<AlertEscalationMO>
{
    private static final long serialVersionUID = 1L;

    /**
     * The alert
     */
    @SQLColumn(index = 1, name = "alert_id", since = @SQLVersion({ 3, 26, 0 }))
    @SQLForeignKey(references = Alert.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.CASCADE, since = @SQLVersion({ 3, 26, 0 }))
    @SQLPrimaryKey()
    private UUID alertId;
    
    /**
     * How long after the alert was this escalation raised
     */
    @SQLColumn(index = 2, name = "after", since = @SQLVersion({ 3, 26, 0 }))
    @SQLPrimaryKey()
    private long after;
    
    /**
     * The unique id of this escalation
     */
    @SQLColumn(index = 3, name = "escalation_id", since = @SQLVersion({ 3, 26, 0 }))
    private UUID escalationId;
    
    /**
     * When did this escalation happen
     */
    @SQLColumn(index = 4, name = "escalated_at", since = @SQLVersion({ 3, 26, 0 }))
    private Timestamp escalatedAt;
    
    /**
     * Who was notified because of this alert escalation
     */
    @SQLColumn(index = 5, name = "notified_ids", type = "UUID[]", since = @SQLVersion({ 3, 26, 0 }))
    private List<UUID> notifiedIds = new LinkedList<UUID>();
    
    public AlertEscalation()
    {
        super();
    }

    public UUID getAlertId()
    {
        return alertId;
    }

    public void setAlertId(UUID alertId)
    {
        this.alertId = alertId;
    }

    public long getAfter()
    {
        return after;
    }

    public void setAfter(long after)
    {
        this.after = after;
    }

    public Timestamp getEscalatedAt()
    {
        return escalatedAt;
    }

    public void setEscalatedAt(Timestamp escalatedAt)
    {
        this.escalatedAt = escalatedAt;
    }

    public List<UUID> getNotifiedIds()
    {
        return notifiedIds;
    }

    public void setNotifiedIds(List<UUID> notifiedIds)
    {
        this.notifiedIds = notifiedIds;
    }
    
    public UUID getEscalationId()
    {
        return escalationId;
    }

    public void setEscalationId(UUID escalationId)
    {
        this.escalationId = escalationId;
    }

    public List<Contact> getNotified()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return this.getNotifiedIds().stream().map((id) -> db.getContact(id)).filter((c) -> c != null).collect(Collectors.toList());
        }
    }

    @Override
    public AlertEscalationMO toMO(Contact contact, EnumSet<com.intrbiz.bergamot.model.BergamotObject.MOFlag> options)
    {
        AlertEscalationMO mo = new AlertEscalationMO();
        mo.setEscalationId(this.getEscalationId());
        mo.setAfter(this.after);
        mo.setEscalatedAt(this.getEscalatedAt().getTime());
        mo.setNotified(this.getNotified().stream().filter((c) -> contact == null || contact.hasPermission("read", c)).map((c) -> c.toStubMO(contact)).collect(Collectors.toList()));
        return mo;
    }
}

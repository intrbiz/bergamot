package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SendAlertAccountingEvent extends BergamotAccountingEvent
{
    public static final UUID TYPE_ID = UUID.fromString("fcafd43f-1b35-43a3-96db-8094f48c664a");
    
    public static enum AlertType { ALERT, RECOVERY, ACKNOWLEDGEMENT }
    
    private UUID alertId;
    
    private UUID checkId;
    
    private AlertType alertType;
    
    private int recipientCount;
    
    public SendAlertAccountingEvent()
    {
        super();
    }
    
    public SendAlertAccountingEvent(long timestamp, UUID siteId, UUID alertId, UUID checkId, AlertType alertType, int recipientCount)
    {
        super(timestamp, siteId);
        this.alertId = alertId;
        this.checkId = checkId;
        this.alertType = alertType;
        this.recipientCount = recipientCount;
    }
    
    public SendAlertAccountingEvent(UUID siteId, UUID alertId, UUID checkId, AlertType alertType, int recipientCount)
    {
        super(siteId);
        this.alertId = alertId;
        this.checkId = checkId;
        this.alertType = alertType;
        this.recipientCount = recipientCount;
    }

    @Override
    public final UUID getTypeId()
    {
        return TYPE_ID;
    }

    public UUID getAlertId()
    {
        return alertId;
    }

    public void setAlertId(UUID alertId)
    {
        this.alertId = alertId;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public AlertType getAlertType()
    {
        return alertType;
    }

    public void setAlertType(AlertType alertType)
    {
        this.alertType = alertType;
    }

    public int getRecipientCount()
    {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount)
    {
        this.recipientCount = recipientCount;
    }

    public String toString()
    {
        return super.toString() + " [" + this.alertId + "] [" + this.checkId + "] [" + this.alertType + "] [" + this.recipientCount + "]";
    }

    @Override
    public void pack(ByteBuffer into)
    {
        super.pack(into);
        this.packUUID(this.alertId, into);
        this.packUUID(this.checkId, into);
        into.putInt(this.alertType == null ? -1 : this.alertType.ordinal());
        into.putInt(this.recipientCount);
    }

    @Override
    public void unpack(ByteBuffer from)
    {
        super.unpack(from);
        this.alertId = this.unpackUUID(from);
        this.checkId = this.unpackUUID(from);
        int rType = from.getInt();
        this.alertType = rType == -1 ? null : AlertType.values()[rType];
        this.recipientCount = from.getInt();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((alertId == null) ? 0 : alertId.hashCode());
        result = prime * result + ((alertType == null) ? 0 : alertType.hashCode());
        result = prime * result + ((checkId == null) ? 0 : checkId.hashCode());
        result = prime * result + recipientCount;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        SendAlertAccountingEvent other = (SendAlertAccountingEvent) obj;
        if (alertId == null)
        {
            if (other.alertId != null) return false;
        }
        else if (!alertId.equals(other.alertId)) return false;
        if (alertType != other.alertType) return false;
        if (checkId == null)
        {
            if (other.checkId != null) return false;
        }
        else if (!checkId.equals(other.checkId)) return false;
        if (recipientCount != other.recipientCount) return false;
        return true;
    }
}

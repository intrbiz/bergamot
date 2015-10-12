package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SendNotificationAccountingEvent extends BergamotAccountingEvent
{
    public static final UUID TYPE_ID = UUID.fromString("fcafd43f-1b35-43a3-96db-8094f48c664a");
    
    public static enum NotificationType { ALERT, RECOVERY, ACKNOWLEDGEMENT, RESET }
    
    private UUID notificationId;
    
    private UUID objectId;
    
    private NotificationType notificationType;
    
    private int recipientCount;
    
    public SendNotificationAccountingEvent()
    {
        super();
    }
    
    public SendNotificationAccountingEvent(long timestamp, UUID siteId, UUID notificationId, UUID objectId, NotificationType notificationType, int recipientCount)
    {
        super(timestamp, siteId);
        this.notificationId = notificationId;
        this.objectId = objectId;
        this.notificationType = notificationType;
        this.recipientCount = recipientCount;
    }
    
    public SendNotificationAccountingEvent(UUID siteId, UUID notificationId, UUID objectId, NotificationType notificationType, int recipientCount)
    {
        super(siteId);
        this.notificationId = notificationId;
        this.objectId = objectId;
        this.notificationType = notificationType;
        this.recipientCount = recipientCount;
    }

    @Override
    public final UUID getTypeId()
    {
        return TYPE_ID;
    }

    public UUID getNotificationId()
    {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId)
    {
        this.notificationId = notificationId;
    }

    public UUID getObjectId()
    {
        return objectId;
    }

    public void setObjectId(UUID objectId)
    {
        this.objectId = objectId;
    }

    public NotificationType getNotificationType()
    {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType)
    {
        this.notificationType = notificationType;
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
        return super.toString() + " [" + this.notificationId + "] [" + this.objectId + "] [" + this.notificationType + "] [" + this.recipientCount + "]";
    }

    @Override
    public void pack(ByteBuffer into)
    {
        super.pack(into);
        this.packUUID(this.notificationId, into);
        this.packUUID(this.objectId, into);
        into.putInt(this.notificationType == null ? -1 : this.notificationType.ordinal());
        into.putInt(this.recipientCount);
    }

    @Override
    public void unpack(ByteBuffer from)
    {
        super.unpack(from);
        this.notificationId = this.unpackUUID(from);
        this.objectId = this.unpackUUID(from);
        int rType = from.getInt();
        this.notificationType = rType == -1 ? null : NotificationType.values()[rType];
        this.recipientCount = from.getInt();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((notificationId == null) ? 0 : notificationId.hashCode());
        result = prime * result + ((notificationType == null) ? 0 : notificationType.hashCode());
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        result = prime * result + recipientCount;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        SendNotificationAccountingEvent other = (SendNotificationAccountingEvent) obj;
        if (notificationId == null)
        {
            if (other.notificationId != null) return false;
        }
        else if (!notificationId.equals(other.notificationId)) return false;
        if (notificationType != other.notificationType) return false;
        if (objectId == null)
        {
            if (other.objectId != null) return false;
        }
        else if (!objectId.equals(other.objectId)) return false;
        if (recipientCount != other.recipientCount) return false;
        return true;
    }
}

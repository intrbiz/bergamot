package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SendNotificationToContactAccountingEvent extends BergamotAccountingEvent
{
    public static final UUID TYPE_ID = UUID.fromString("01fb5dd1-e69f-403c-8847-c3a3f572f8d8");
    
    private UUID notificationId;
    
    private UUID objectId;
    
    private AccountingNotificationType notificationType;
    
    private UUID contact;
    
    private String engine;
    
    private String messageType;
    
    private String messageAddress;
    
    private String messageId;
    
    public SendNotificationToContactAccountingEvent()
    {
        super();
    }
    
    public SendNotificationToContactAccountingEvent(long timestamp, UUID siteId, UUID notificationId, UUID objectId, AccountingNotificationType notificationType, UUID contact, String engine, String messageType, String messageAddress, String messageId)
    {
        super(timestamp, siteId);
        this.notificationId = notificationId;
        this.objectId = objectId;
        this.notificationType = notificationType;
        this.contact = contact;
        this.engine = engine;
        this.messageType = messageType;
        this.messageAddress = messageAddress;
        this.messageId = messageId;
    }
    
    public SendNotificationToContactAccountingEvent(UUID siteId, UUID notificationId, UUID objectId, AccountingNotificationType notificationType, UUID contact, String engine, String messageType, String messageAddress, String messageId)
    {
        super(siteId);
        this.notificationId = notificationId;
        this.objectId = objectId;
        this.notificationType = notificationType;
        this.contact = contact;
        this.engine = engine;
        this.messageType = messageType;
        this.messageAddress = messageAddress;
        this.messageId = messageId;
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

    public AccountingNotificationType getNotificationType()
    {
        return notificationType;
    }

    public void setNotificationType(AccountingNotificationType notificationType)
    {
        this.notificationType = notificationType;
    }

    public UUID getContact()
    {
        return contact;
    }

    public void setContact(UUID contact)
    {
        this.contact = contact;
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public String getMessageAddress()
    {
        return messageAddress;
    }

    public void setMessageAddress(String messageAddress)
    {
        this.messageAddress = messageAddress;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    public String toString()
    {
        return super.toString() + " [" + this.notificationId + "] [" + this.objectId + "] [" + this.notificationType + "] [" + this.contact + "] [" + this.engine + "] [" + this.messageType + "] [" + this.messageAddress + "] [" + this.messageId + "]";
    }

    @Override
    public void pack(ByteBuffer into)
    {
        super.pack(into);
        this.packUUID(this.notificationId, into);
        this.packUUID(this.objectId, into);
        into.putInt(this.notificationType == null ? -1 : this.notificationType.ordinal());
        this.packUUID(this.contact, into);
        this.packString(this.engine, into);
        this.packString(this.messageType, into);
        this.packString(this.messageAddress, into);
        this.packString(this.messageId, into);
    }

    @Override
    public void unpack(ByteBuffer from)
    {
        super.unpack(from);
        this.notificationId = this.unpackUUID(from);
        this.objectId = this.unpackUUID(from);
        int rType = from.getInt();
        this.notificationType = rType == -1 ? null : AccountingNotificationType.values()[rType];
        this.contact = this.unpackUUID(from);
        this.engine = this.unpackString(from);
        this.messageType = this.unpackString(from);
        this.messageAddress = this.unpackString(from);
        this.messageId = this.unpackString(from);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + ((engine == null) ? 0 : engine.hashCode());
        result = prime * result + ((messageAddress == null) ? 0 : messageAddress.hashCode());
        result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
        result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
        result = prime * result + ((notificationId == null) ? 0 : notificationId.hashCode());
        result = prime * result + ((notificationType == null) ? 0 : notificationType.hashCode());
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        SendNotificationToContactAccountingEvent other = (SendNotificationToContactAccountingEvent) obj;
        if (contact == null)
        {
            if (other.contact != null) return false;
        }
        else if (!contact.equals(other.contact)) return false;
        if (engine == null)
        {
            if (other.engine != null) return false;
        }
        else if (!engine.equals(other.engine)) return false;
        if (messageAddress == null)
        {
            if (other.messageAddress != null) return false;
        }
        else if (!messageAddress.equals(other.messageAddress)) return false;
        if (messageId == null)
        {
            if (other.messageId != null) return false;
        }
        else if (!messageId.equals(other.messageId)) return false;
        if (messageType == null)
        {
            if (other.messageType != null) return false;
        }
        else if (!messageType.equals(other.messageType)) return false;
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
        return true;
    }
}

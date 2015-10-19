package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SignAgentAccountingEvent extends BergamotAccountingEvent
{
    public static final UUID TYPE_ID = UUID.fromString("fdbc3042-d4a4-45bb-a54f-0ae8da9c6d44");
    
    private UUID agentId;
    
    private String commonName;
    
    private String serial;
    
    private UUID contact;
    
    public SignAgentAccountingEvent()
    {
        super();
    }
    
    public SignAgentAccountingEvent(long timestamp, UUID siteId, UUID agentId, String commonName, String serial, UUID contact)
    {
        super(timestamp, siteId);
        this.agentId = agentId;
        this.commonName = commonName;
        this.serial = serial;
        this.contact = contact;
    }
    
    public SignAgentAccountingEvent(UUID siteId, UUID agentId, String commonName, String serial, UUID contact)
    {
        super(siteId);
        this.agentId = agentId;
        this.commonName = commonName;
        this.serial = serial;
        this.contact = contact;
    }

    @Override
    public final UUID getTypeId()
    {
        return TYPE_ID;
    }

    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    public String getSerial()
    {
        return serial;
    }

    public void setSerial(String serial)
    {
        this.serial = serial;
    }

    public UUID getContact()
    {
        return contact;
    }

    public void setContact(UUID contact)
    {
        this.contact = contact;
    }

    public String toString()
    {
        return super.toString() + " [" + this.agentId + "] [" + this.commonName + "] [" + this.serial + "] [" + this.contact + "]";
    }

    @Override
    public void pack(ByteBuffer into)
    {
        super.pack(into);
        this.packUUID(this.agentId, into);
        this.packString(this.commonName, into);
        this.packString(this.serial, into);
        this.packUUID(this.contact, into);
    }

    @Override
    public void unpack(ByteBuffer from)
    {
        super.unpack(from);
        this.agentId = this.unpackUUID(from);
        this.commonName = this.unpackString(from);
        this.serial = this.unpackString(from);
        this.contact = this.unpackUUID(from);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        result = prime * result + ((commonName == null) ? 0 : commonName.hashCode());
        result = prime * result + ((contact == null) ? 0 : contact.hashCode());
        result = prime * result + ((serial == null) ? 0 : serial.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        SignAgentAccountingEvent other = (SignAgentAccountingEvent) obj;
        if (agentId == null)
        {
            if (other.agentId != null) return false;
        }
        else if (!agentId.equals(other.agentId)) return false;
        if (commonName == null)
        {
            if (other.commonName != null) return false;
        }
        else if (!commonName.equals(other.commonName)) return false;
        if (contact == null)
        {
            if (other.contact != null) return false;
        }
        else if (!contact.equals(other.contact)) return false;
        if (serial == null)
        {
            if (other.serial != null) return false;
        }
        else if (!serial.equals(other.serial)) return false;
        return true;
    }
}

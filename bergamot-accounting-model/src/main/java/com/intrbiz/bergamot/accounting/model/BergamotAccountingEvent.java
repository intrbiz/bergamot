package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.accounting.model.AccountingEvent;

public abstract class BergamotAccountingEvent implements AccountingEvent
{   
    private long timestamp;
    
    private UUID siteId;
    
    public BergamotAccountingEvent()
    {
        super();
    }
    
    public BergamotAccountingEvent(long timestamp, UUID siteId)
    {
        super();
        this.timestamp = timestamp;
        this.siteId = siteId;
    }
    
    public BergamotAccountingEvent(UUID siteId)
    {
        this(System.currentTimeMillis(), siteId);
    }

    @Override
    public final long getTimestamp()
    {
        return this.timestamp;
    }

    public final void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public final UUID getSiteId()
    {
        return siteId;
    }

    public final void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }
    
    public String toString()
    {
        return "[" + this.siteId + "]";
    }
    
    public void pack(ByteBuffer into)
    {
        into.putLong(this.timestamp);
        this.packUUID(this.siteId, into);
    }
    
    public void unpack(ByteBuffer from)
    {
        this.timestamp = from.getLong();
        this.siteId = this.unpackUUID(from);
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BergamotAccountingEvent other = (BergamotAccountingEvent) obj;
        if (siteId == null)
        {
            if (other.siteId != null) return false;
        }
        else if (!siteId.equals(other.siteId)) return false;
        if (timestamp != other.timestamp) return false;
        return true;
    }

    protected void packUUID(UUID id, ByteBuffer into)
    {
        into.putLong(id == null ? -1L : id.getMostSignificantBits());
        into.putLong(id == null ? -1L : id.getLeastSignificantBits());
    }
    
    protected UUID unpackUUID(ByteBuffer from)
    {
        long msb = from.getLong();
        long lsb = from.getLong();
        return msb == -1L && lsb == -1L ? null : new UUID(msb, lsb);
    }
    
    protected void packString(String str, ByteBuffer into)
    {
        if (str == null)
        {
            into.putShort((short) 0xFFFF);
        }
        else
        {
            byte[] bytes = str.getBytes(Util.UTF8);
            if (bytes.length > 0x7FFF) throw new IllegalArgumentException("Strings longer than " + 0x7FFF + " are not supported");
            into.putShort((short) (bytes.length & 0x7FFF));
            into.put(bytes);
        }
    }
    
    protected String unpackString(ByteBuffer from)
    {
        short len = from.getShort();
        if (len == ((short) 0xFFFF)) return null;
        byte[] bytes = new byte[len];
        from.get(bytes);
        return new String(bytes, Util.UTF8);
    }
}

package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

public class LoginAccountingEvent extends BergamotAccountingEvent
{
    public static final UUID TYPE_ID = UUID.fromString("c3f43c54-e8a0-45ce-8213-fa71221ae5fc");
    
    private UUID contactId;
    
    private String host;
    
    private String username;
    
    private String sessionId;
    
    private boolean autoLogin;
    
    private boolean success;
    
    private String remoteAddress;
    
    public LoginAccountingEvent()
    {
        super();
    }
    
    public LoginAccountingEvent(long timestamp, UUID siteId, UUID contactId, String host, String username, String sessionId, boolean autoLogin, boolean success, String remoteAddress)
    {
        super(timestamp, siteId);
        this.contactId = contactId;
        this.host = host;
        this.username = username;
        this.sessionId = sessionId;
        this.autoLogin = autoLogin;
        this.success = success;
        this.remoteAddress = remoteAddress;
    }
    
    public LoginAccountingEvent(UUID siteId, UUID contactId, String host, String username, String sessionId, boolean autoLogin, boolean success, String remoteAddress)
    {
        super(siteId);
        this.contactId = contactId;
        this.host = host;
        this.username = username;
        this.sessionId = sessionId;
        this.autoLogin = autoLogin;
        this.success = success;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public final UUID getTypeId()
    {
        return TYPE_ID;
    }

    public UUID getContactId()
    {
        return contactId;
    }

    public void setContactId(UUID contactId)
    {
        this.contactId = contactId;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public boolean isAutoLogin()
    {
        return autoLogin;
    }

    public void setAutoLogin(boolean autoLogin)
    {
        this.autoLogin = autoLogin;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getRemoteAddress()
    {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress)
    {
        this.remoteAddress = remoteAddress;
    }

    public String toString()
    {
        return super.toString() + " [" + this.contactId + "] [" + this.host + "] [" + this.username + "] [" + this.sessionId + "] [" + this.autoLogin + "] [" + this.success + "] [" + this.remoteAddress + "]";
    }

    @Override
    public void pack(ByteBuffer into)
    {
        super.pack(into);
        this.packUUID(this.contactId, into);
        this.packString(this.host, into);
        this.packString(username, into);
        this.packString(sessionId, into);
        into.put((byte) (this.autoLogin ? 1 : 0));
        into.put((byte) (this.success ? 1 : 0));
        this.packString(this.remoteAddress, into);
    }

    @Override
    public void unpack(ByteBuffer from)
    {
        super.unpack(from);
        this.contactId = this.unpackUUID(from);
        this.host = this.unpackString(from);
        this.username = this.unpackString(from);
        this.sessionId = this.unpackString(from);
        this.autoLogin = from.get() == 1;
        this.success = from.get() == 1;
        this.remoteAddress = this.unpackString(from);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (autoLogin ? 1231 : 1237);
        result = prime * result + ((contactId == null) ? 0 : contactId.hashCode());
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        result = prime * result + (success ? 1231 : 1237);
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        LoginAccountingEvent other = (LoginAccountingEvent) obj;
        if (autoLogin != other.autoLogin) return false;
        if (contactId == null)
        {
            if (other.contactId != null) return false;
        }
        else if (!contactId.equals(other.contactId)) return false;
        if (host == null)
        {
            if (other.host != null) return false;
        }
        else if (!host.equals(other.host)) return false;
        if (sessionId == null)
        {
            if (other.sessionId != null) return false;
        }
        else if (!sessionId.equals(other.sessionId)) return false;
        if (success != other.success) return false;
        if (username == null)
        {
            if (other.username != null) return false;
        }
        else if (!username.equals(other.username)) return false;
        return true;
    }
}

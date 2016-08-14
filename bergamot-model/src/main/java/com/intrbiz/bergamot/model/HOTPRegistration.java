package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.util.CounterHOTP.CounterHOTPState;
import com.intrbiz.util.HOTP.HOTPSecret;

/**
 * A (counter) HOTP based system which can been configured for the contract
 */
@SQLTable(schema = BergamotDB.class, name = "hotp_registration", since = @SQLVersion({ 3, 39, 0 }))
public class HOTPRegistration implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 3,39, 0 }))
    @SQLPrimaryKey
    private UUID id;
    
    /**
     * The contact whom this token represents
     */
    @SQLColumn(index = 2, name = "contact_id", since = @SQLVersion({ 3, 39, 0 }))
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    private UUID contactId;
    
    @SQLColumn(index = 3, name = "secret", notNull = true, since = @SQLVersion({ 3, 39, 0 }))
    private byte[] secret;
    
    @SQLColumn(index = 4, name = "counter", notNull = true, since = @SQLVersion({ 3, 39, 0 }))
    private long counter;
    
    /**
     * When did we register this device
     */
    @SQLColumn(index = 5, name = "created", since = @SQLVersion({ 3, 39, 0 }))
    private Timestamp created = new Timestamp(System.currentTimeMillis());
    
    /**
     * When was this last updated
     */
    @SQLColumn(index = 6, name = "updated", since = @SQLVersion({ 3, 39, 0 }))
    private Timestamp updated = null;
    
    /**
     * Has this device been revoked
     */
    @SQLColumn(index = 7, name = "revoked", since = @SQLVersion({ 3, 39, 0 }))
    private boolean revoked = false;
    
    /**
     * When was it revoked
     */
    @SQLColumn(index = 8, name = "revoked_at", since = @SQLVersion({ 3, 39, 0 }))
    private Timestamp revokedAt = null;
    
    /**
     * A name for this method
     */
    @SQLColumn(index = 9, name = "summary", since = @SQLVersion({ 3, 39, 0 }))
    private String summary;
    
    public HOTPRegistration()
    {
        super();
    }
    
    public HOTPRegistration(Contact contact, HOTPSecret secret, String summary)
    {
        this.id = Site.randomId(contact.getSiteId());
        this.contactId = contact.getId();
        this.created = new Timestamp(System.currentTimeMillis());
        this.updated = null;
        this.revoked = false;
        this.secret = secret.getSecret();
        this.counter = 0L;
        this.summary = summary;
    }

    public UUID getContactId()
    {
        return contactId;
    }

    public void setContactId(UUID contactId)
    {
        this.contactId = contactId;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public byte[] getSecret()
    {
        return secret;
    }

    public void setSecret(byte[] secret)
    {
        this.secret = secret;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public long getCounter()
    {
        return counter;
    }

    public void setCounter(long counter)
    {
        this.counter = counter;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public Timestamp getUpdated()
    {
        return updated;
    }

    public void setUpdated(Timestamp updated)
    {
        this.updated = updated;
    }

    public boolean isRevoked()
    {
        return revoked;
    }

    public void setRevoked(boolean revoked)
    {
        this.revoked = revoked;
    }

    public Timestamp getRevokedAt()
    {
        return revokedAt;
    }

    public void setRevokedAt(Timestamp revokedAt)
    {
        this.revokedAt = revokedAt;
    }

    public Contact getContact()
    {
        if (this.getContactId() == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getContact(this.getContactId());
        }
    }
    
    public HOTPSecret toHOTPSecret()
    {
        return new HOTPSecret(this.secret);
    }
    
    public CounterHOTPState toHOTPState()
    {
        return new CounterHOTPState(this.counter);
    }
    
    public HOTPRegistration revoke()
    {
        this.revoked = true;
        this.revokedAt = new Timestamp(System.currentTimeMillis());
        return this;
    }
    
    public HOTPRegistration used(CounterHOTPState newState)
    {
        this.counter = newState.getCounter();
        this.updated = new Timestamp(System.currentTimeMillis());
        return this;
    }
    
    public String toString()
    {
        return "HOTPToken { secret=" + Arrays.toString(this.secret) + ", counter=" + this.counter + "}";
    }
}

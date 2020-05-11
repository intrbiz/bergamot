package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A perpetual API token which has been created for a Contact
 */
@SQLTable(schema = BergamotDB.class, name = "api_token", since = @SQLVersion({4, 0, 0}))
public class APIToken implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The API Token which will be used for perpetual authentication of external things
     */
    @SQLColumn(index = 1, name = "token", since = @SQLVersion({4, 0, 0}))
    @SQLPrimaryKey
    private String token;
    
    /**
     * The contact whom this token represents
     */
    @SQLColumn(index = 2, name = "contact_id", since = @SQLVersion({4, 0, 0}))
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({4, 0, 0}))
    private UUID contactId;
    
    @SQLColumn(index = 3, name = "summary", notNull = true, since = @SQLVersion({4, 0, 0}))
    private String summary;

    @SQLColumn(index = 4, name = "description", since = @SQLVersion({4, 0, 0}))
    private String description;
    
    @SQLColumn(index = 5, name = "created", since = @SQLVersion({4, 0, 0}))
    private Timestamp created = new Timestamp(System.currentTimeMillis());
    
    @SQLColumn(index = 6, name = "updated", since = @SQLVersion({4, 0, 0}))
    private Timestamp updated = new Timestamp(System.currentTimeMillis());
    
    /**
     * Has this token been revoked
     */
    @SQLColumn(index = 7, name = "revoked", since = @SQLVersion({4, 0, 0}))
    private boolean revoked = false;
    
    /**
     * When was it revoked
     */
    @SQLColumn(index = 8, name = "revoked_at", since = @SQLVersion({4, 0, 0}))
    private Timestamp revokedAt = null;
    
    public APIToken()
    {
        super();
    }
    
    public APIToken(String token, Contact contact, String summary)
    {
        this();
        this.token = token;
        this.contactId = contact.getId();
        this.summary = summary;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public UUID getContactId()
    {
        return contactId;
    }

    public void setContactId(UUID contactId)
    {
        this.contactId = contactId;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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
    
    public APIToken revoke()
    {
        this.revoked = true;
        this.revokedAt = new Timestamp(System.currentTimeMillis());
        return this;
    }
    
    public String toString()
    {
        return "APIToken { " + this.token + "}";
    }
}

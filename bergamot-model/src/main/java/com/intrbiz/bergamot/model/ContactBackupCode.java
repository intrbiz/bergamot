package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.codec.binary.Base32;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A single use backup code for as a two factor auth fall back
 */
@SQLTable(schema = BergamotDB.class, name = "backup_code", since = @SQLVersion({ 3, 40, 0 }))
public class ContactBackupCode implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 3,40, 0 }))
    @SQLPrimaryKey
    private UUID id;
    
    /**
     * The contact whom this backup code is for
     */
    @SQLColumn(index = 2, name = "contact_id", since = @SQLVersion({ 3, 40, 0 }))
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    private UUID contactId;
    
    @SQLColumn(index = 3, name = "code", notNull = true, since = @SQLVersion({ 3, 40, 0 }))
    private String code;
    
    /**
     * When did we create this code
     */
    @SQLColumn(index = 4, name = "created", since = @SQLVersion({ 3, 40, 0 }))
    private Timestamp created = new Timestamp(System.currentTimeMillis());
    
    /**
     * When was this last updated
     */
    @SQLColumn(index = 5, name = "updated", since = @SQLVersion({ 3, 40, 0 }))
    private Timestamp updated = null;
    
    /**
     * Was this code used
     */
    @SQLColumn(index = 6, name = "used", since = @SQLVersion({ 3, 40, 0 }))
    private boolean used = false;
    
    /**
     * When was it used
     */
    @SQLColumn(index = 7, name = "used_at", since = @SQLVersion({ 3, 40, 0 }))
    private Timestamp usedAt = null;
    
    public ContactBackupCode()
    {
        super();
    }
    
    public ContactBackupCode(Contact contact)
    {
        this.id = Site.randomId(contact.getSiteId());
        this.contactId = contact.getId();
        this.created = new Timestamp(System.currentTimeMillis());
        this.updated = null;
        this.used = false;
        this.usedAt = null;
        this.code = generateNewBackupCode();
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
    
    

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public boolean isUsed()
    {
        return used;
    }

    public void setUsed(boolean used)
    {
        this.used = used;
    }

    public Timestamp getUsedAt()
    {
        return usedAt;
    }

    public void setUsedAt(Timestamp usedAt)
    {
        this.usedAt = usedAt;
    }

    public Contact getContact()
    {
        if (this.getContactId() == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getContact(this.getContactId());
        }
    }
    
    public ContactBackupCode used()
    {
        this.used = true;
        this.usedAt = new Timestamp(System.currentTimeMillis());
        this.updated = this.usedAt;
        return this;
    }
    
    public String toString()
    {
        return "BackupCode { code=" + this.code + ", used=" + this.used + "}";
    }
    
    public static final String generateNewBackupCode()
    {
        byte[] key = new byte[5];
        (new SecureRandom()).nextBytes(key);
        StringBuilder sb = new StringBuilder();
        char[] token = (new Base32()).encodeToString(key).toLowerCase().toCharArray();
        for (int i = 0; i < token.length; i++)
        {
            if (i > 0 && (i % 4) == 0) sb.append("-");
            sb.append(token[i]);
        }
        return sb.toString();
    }
}

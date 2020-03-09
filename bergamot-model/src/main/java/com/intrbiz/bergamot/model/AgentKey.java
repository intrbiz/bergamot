package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A key used to authenticate Agents against this site
 */
@SQLTable(schema = BergamotDB.class, name = "agent_key", since = @SQLVersion({ 4, 0, 2 }))
public class AgentKey implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public static final int DEFAULT_KEY_SIZE = 32;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 4, 0, 2 }))
    @SQLPrimaryKey()
    protected UUID id;

    @SQLColumn(index = 2, name = "site_id", notNull = true, since = @SQLVersion({ 4, 0, 2 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 4, 0, 2 }))
    protected UUID siteId;
    
    @SQLColumn(index = 3, name = "purpose", notNull = true, since = @SQLVersion({ 4, 0, 2 }))
    private String purpose;
    
    /**
     * The secret key used for authentication
     */
    @SQLColumn(index = 4, name = "secret", since = @SQLVersion({ 4, 0, 2 }))
    @SQLPrimaryKey
    private byte[] secret;
    
    /**
     * When this key was created
     */
    @SQLColumn(index = 5, name = "created", since = @SQLVersion({ 4, 0, 2 }))
    private Timestamp created = new Timestamp(System.currentTimeMillis());
    
    /**
     * Has this token been revoked
     */
    @SQLColumn(index = 6, name = "revoked", since = @SQLVersion({ 4, 0, 2 }))
    private boolean revoked = false;
    
    /**
     * When was it revoked
     */
    @SQLColumn(index = 7, name = "revoked_at", since = @SQLVersion({ 4, 0, 2 }))
    private Timestamp revokedAt = null;
    
    public AgentKey()
    {
        super();
    }
    
    public UUID getId()
    {
        return this.id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getSiteId()
    {
        return this.siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public String getPurpose()
    {
        return this.purpose;
    }

    public void setPurpose(String purpose)
    {
        this.purpose = purpose;
    }

    public byte[] getSecret()
    {
        return this.secret;
    }

    public void setSecret(byte[] secret)
    {
        this.secret = secret;
    }

    public Timestamp getCreated()
    {
        return this.created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public boolean isRevoked()
    {
        return this.revoked;
    }

    public void setRevoked(boolean revoked)
    {
        this.revoked = revoked;
    }

    public Timestamp getRevokedAt()
    {
        return this.revokedAt;
    }

    public void setRevokedAt(Timestamp revokedAt)
    {
        this.revokedAt = revokedAt;
    }

    public AgentKey revoke()
    {
        this.revoked = true;
        this.revokedAt = new Timestamp(System.currentTimeMillis());
        return this;
    }
    
    public AgentAuthenticationKey toAgentAuthenticationKey()
    {
        return new AgentAuthenticationKey(this.id, this.secret);
    }
    
    public String toString()
    {
        return "AgentKey { " + this.id + "}";
    }
    
    public static final AgentKey create(UUID siteId, String purpose)
    {
        AgentKey key = new AgentKey();
        key.setId(Site.randomId(siteId));
        key.setSiteId(siteId);
        key.setPurpose(purpose);
        key.setCreated(new Timestamp(System.currentTimeMillis()));
        key.setRevoked(false);
        key.setRevokedAt(null);
        // generate the scret
        byte[] secret = new byte[DEFAULT_KEY_SIZE];
        (new SecureRandom()).nextBytes(secret);
        key.setSecret(secret);
        return key;
    }
}

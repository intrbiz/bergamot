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
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;


/**
 * A log of registered Bergamot Agents
 * 
 * Note: we don't store the certificate / key material here nor the agent configuration file.
 * 
 */
@SQLTable(schema = BergamotDB.class, name = "agent_registration", since = @SQLVersion({ 2, 2, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class AgentRegistration implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 2, 2, 0 }))
    @SQLPrimaryKey()
    protected UUID id;

    @SQLColumn(index = 2, name = "site_id", notNull = true, since = @SQLVersion({ 2, 2, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 2, 2, 0 }))
    protected UUID siteId;

    @SQLColumn(index = 3, name = "name", notNull = true, since = @SQLVersion({ 2, 2, 0 }))
    protected String name;

    @SQLColumn(index = 4, name = "summary", notNull = true, since = @SQLVersion({ 2, 2, 0 }))
    protected String summary;

    @SQLColumn(index = 5, name = "description", since = @SQLVersion({ 2, 2, 0 }))
    protected String description;

    @SQLColumn(index = 6, name = "created", since = @SQLVersion({ 2, 2, 0 }))
    protected Timestamp created = new Timestamp(System.currentTimeMillis());

    @SQLColumn(index = 7, name = "updated", since = @SQLVersion({ 2, 2, 0 }))
    protected Timestamp updated = new Timestamp(System.currentTimeMillis());
    
    @SQLColumn(index = 8, name = "revoked", since = @SQLVersion({ 2, 2, 0 }))
    protected boolean revoked;
    
    @SQLColumn(index = 9, name = "revoked_on", since = @SQLVersion({ 2, 2, 0 }))
    protected Timestamp revokedOn;
    
    /**
     * Hex encoded serial number of the current certificate
     */
    @SQLColumn(index = 10, name = "certificate_serial", since = @SQLVersion({ 2, 2, 0 }))
    protected String certificateSerial;
    
    public AgentRegistration()
    {
        super();
    }
    
    public AgentRegistration(UUID siteId, UUID agentId, String commonName, String serial)
    {
        this.siteId = siteId;
        this.id = agentId;
        this.name = commonName;
        this.summary = commonName;
        this.certificateSerial = serial;
        this.revoked = false;
        this.revokedOn = null;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public Timestamp getRevokedOn()
    {
        return revokedOn;
    }

    public void setRevokedOn(Timestamp revokedOn)
    {
        this.revokedOn = revokedOn;
    }

    public String getCertificateSerial()
    {
        return certificateSerial;
    }

    public void setCertificateSerial(String certificateSerial)
    {
        this.certificateSerial = certificateSerial;
    }
}

package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "computed_permissions_for_domain", since = @SQLVersion({4, 0, 0}) )
public class ComputedPermissionForDomain implements Serializable
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "contact_id", since = @SQLVersion({4, 0, 0}) )
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({4, 0, 0}) )
    @SQLPrimaryKey
    private UUID contactId;

    @SQLColumn(index = 2, name = "security_domain_id", since = @SQLVersion({4, 0, 0}) )
    @SQLForeignKey(references = SecurityDomain.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({4, 0, 0}) )
    @SQLPrimaryKey
    private UUID securityDomainId;

    @SQLColumn(index = 3, name = "permission", since = @SQLVersion({4, 0, 0}) )
    @SQLPrimaryKey
    private String permission;

    @SQLColumn(index = 4, name = "allowed", since = @SQLVersion({4, 0, 0}) )
    private boolean allowed;

    public ComputedPermissionForDomain()
    {
        super();
    }

    public UUID getContactId()
    {
        return contactId;
    }

    public void setContactId(UUID contactId)
    {
        this.contactId = contactId;
    }

    public UUID getSecurityDomainId()
    {
        return securityDomainId;
    }

    public void setSecurityDomainId(UUID securityDomainId)
    {
        this.securityDomainId = securityDomainId;
    }

    public String getPermission()
    {
        return permission;
    }

    public void setPermission(String permission)
    {
        this.permission = permission;
    }

    public boolean isAllowed()
    {
        return allowed;
    }

    public void setAllowed(boolean allowed)
    {
        this.allowed = allowed;
    }
}

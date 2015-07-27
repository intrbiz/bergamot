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

@SQLTable(schema = BergamotDB.class, name = "security_domain_membership", since = @SQLVersion({ 3, 8, 0 }))
public class SecurityDomainMembership implements Serializable
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "security_domain_id", since = @SQLVersion({ 3, 8, 0 }))
    @SQLForeignKey(references = SecurityDomain.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 3, 8, 0 }))
    @SQLPrimaryKey
    private UUID securityDomainId;
    
    @SQLColumn(index = 2, name = "check_id", since = @SQLVersion({ 3, 8, 0 }))
    @SQLPrimaryKey
    private UUID checkId;
    
    public SecurityDomainMembership()
    {
        super();
    }
    
    public SecurityDomainMembership(UUID securityDomainId, UUID checkId)
    {
        this.securityDomainId = securityDomainId;
        this.checkId = checkId;
    }

    public UUID getSecurityDomainId()
    {
        return securityDomainId;
    }

    public void setSecurityDomainId(UUID securityDomainId)
    {
        this.securityDomainId = securityDomainId;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }
}

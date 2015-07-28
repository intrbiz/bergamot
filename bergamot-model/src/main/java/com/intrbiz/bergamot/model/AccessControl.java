package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "access_control", since = @SQLVersion({ 3, 8, 0 }))
public class AccessControl implements Serializable
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "security_domain_id", since = @SQLVersion({ 3, 8, 0 }))
    @SQLForeignKey(references = SecurityDomain.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 3, 8, 0 }))
    @SQLPrimaryKey
    private UUID securityDomainId;
    
    /**
     * The role (contact or team) this access control grants or revokes on
     */
    @SQLColumn(index = 2, name = "role_id", since = @SQLVersion({ 3, 8, 0 }))
    @SQLPrimaryKey
    private UUID roleId;
    
    @SQLColumn(index = 7, name = "granted_permissions", type = "TEXT[]", since = @SQLVersion({ 3, 8, 0 }))
    private List<String> grantedPermissions = new LinkedList<String>();

    @SQLColumn(index = 8, name = "revoked_permissions", type = "TEXT[]", since = @SQLVersion({ 3, 8, 0 }))
    private List<String> revokedPermissions = new LinkedList<String>();
    
    public AccessControl()
    {
        super();
    }
    
    public AccessControl(UUID securityDomainId, UUID roleId, List<String> grantedPermissions, List<String> revokedPermissions)
    {
        this.securityDomainId = securityDomainId;
        this.roleId = roleId;
        this.grantedPermissions = grantedPermissions;
        this.revokedPermissions = revokedPermissions;
    }

    public UUID getSecurityDomainId()
    {
        return securityDomainId;
    }

    public void setSecurityDomainId(UUID securityDomainId)
    {
        this.securityDomainId = securityDomainId;
    }

    public UUID getRoleId()
    {
        return roleId;
    }

    public void setRoleId(UUID roleId)
    {
        this.roleId = roleId;
    }

    public List<String> getGrantedPermissions()
    {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<String> grantedPermissions)
    {
        this.grantedPermissions = grantedPermissions;
    }

    public List<String> getRevokedPermissions()
    {
        return revokedPermissions;
    }

    public void setRevokedPermissions(List<String> revokedPermissions)
    {
        this.revokedPermissions = revokedPermissions;
    }
}

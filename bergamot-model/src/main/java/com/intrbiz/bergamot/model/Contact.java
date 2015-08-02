package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "contact", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class Contact extends NamedObject<ContactMO, ContactCfg> implements Principal, Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int BCRYPT_WORK_FACTOR = 12;
    
    public static enum LockOutReason
    { 
        ADMINISTRATIVE("Administratively locked"), 
        AUTOMATIC("Automatically locked");
        
        private final String description;
        
        private LockOutReason(String description)
        {
            this.description = description;
        }
        
        public String getDescription()
        {
            return this.description;
        }
    }

    @SQLColumn(index = 1, name = "team_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<UUID> teamIds = new LinkedList<UUID>();

    @SQLColumn(index = 2, name = "password_hash", since = @SQLVersion({ 1, 0, 0 }))
    private String passwordHash;

    @SQLColumn(index = 3, name = "email", since = @SQLVersion({ 1, 0, 0 }))
    @SQLUnique(name = "email_unq", columns = { "site_id", "email" })
    private String email;

    @SQLColumn(index = 4, name = "pager", since = @SQLVersion({ 1, 0, 0 }))
    private String pager;

    @SQLColumn(index = 5, name = "mobile", since = @SQLVersion({ 1, 0, 0 }))
    private String mobile;

    @SQLColumn(index = 6, name = "phone", since = @SQLVersion({ 1, 0, 0 }))
    private String phone;

    @SQLColumn(index = 7, name = "granted_permissions", type = "TEXT[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<String> grantedPermissions = new LinkedList<String>();

    @SQLColumn(index = 8, name = "revoked_permissions", type = "TEXT[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<String> revokedPermissions = new LinkedList<String>();
    
    // account security flags
    
    /**
     * Should the password be changed on next login
     */
    @SQLColumn(index = 9, name = "force_password_change", since = @SQLVersion({ 1, 0, 0 }))
    private boolean forcePasswordChange = false;
    
    /**
     * Is this account locked in any way, IE: reject authentication even with valid credentials
     */
    @SQLColumn(index = 10, name = "locked", since = @SQLVersion({ 1, 0, 0 }))
    private boolean locked = false;
    
    /**
     * Why the account was locked
     */
    @SQLColumn(index = 11, name = "locked_reason", since = @SQLVersion({ 1, 0, 0 }))
    private LockOutReason lockedReason;
    
    /**
     * When this account was locked last
     */
    @SQLColumn(index = 12, name = "locked_at", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp lockedAt = null;

    public Contact()
    {
        super();
    }

    @Override
    public void configure(ContactCfg configuration, ContactCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        // email
        this.email  = resolvedConfiguration.getEmail();
        // phones
        this.mobile = resolvedConfiguration.getMobile();
        this.pager  = resolvedConfiguration.getPager();
        this.phone  = resolvedConfiguration.getPhone();
        // permissions
        this.grantedPermissions.clear();
        this.grantedPermissions.addAll(resolvedConfiguration.getGrantedPermissions());
        this.revokedPermissions.clear();
        this.revokedPermissions.addAll(resolvedConfiguration.getRevokedPermissions());
    }

    public List<UUID> getTeamIds()
    {
        return this.teamIds;
    }

    public void setTeamIds(List<UUID> teamIds)
    {
        this.teamIds = teamIds;
    }

    public List<Team> getTeams()
    {
        List<Team> r = new LinkedList<Team>();
        try (BergamotDB db = BergamotDB.connect())
        {
            for (UUID id : this.getTeamIds())
            {
                r.add(db.getTeam(id));
            }
        }
        return r;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }

    public void hashPassword(String plainPassword)
    {
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
        // reset as we've updated the password
        this.forcePasswordChange = false;
    }

    public boolean verifyPassword(String plainPassword)
    {
        if (plainPassword == null || this.passwordHash == null) return false;
        return BCrypt.checkpw(plainPassword, this.passwordHash);
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPager()
    {
        return pager;
    }

    public void setPager(String pager)
    {
        this.pager = pager;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Notifications getNotifications()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getNotifications(this.getId());
        }
    }

    public void setNotifications(Notifications notifications)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            notifications.setId(this.getId());
            db.setNotifications(notifications);
        }
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

    public boolean isForcePasswordChange()
    {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange)
    {
        this.forcePasswordChange = forcePasswordChange;
    }

    public boolean isLocked()
    {
        return locked;
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }

    public LockOutReason getLockedReason()
    {
        return lockedReason;
    }

    public void setLockedReason(LockOutReason lockedReason)
    {
        this.lockedReason = lockedReason;
    }

    public Timestamp getLockedAt()
    {
        return lockedAt;
    }

    public void setLockedAt(Timestamp lockedAt)
    {
        this.lockedAt = lockedAt;
    }
    
    public Contact forcePasswordChange()
    {
        this.forcePasswordChange = true;
        return this;
    }
    
    public Contact resetPassword()
    {
        this.passwordHash = null;
        this.forcePasswordChange = true;
        return this;
    }
    
    public Contact lock(LockOutReason reason)
    {
        this.locked = true;
        this.lockedReason = reason;
        this.lockedAt = new Timestamp(System.currentTimeMillis());
        return this;
    }
    
    public Contact unlock()
    {
        this.locked = false;
        this.lockedReason = null;
        this.lockedAt = null;
        return this;
    }
    
    public List<APIToken> getAPITokens()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAPITokensForContact(this.getId());
        }
    }
    
    public List<AccessControl> getAccessControls()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAccessControlsForRole(this.getId());
        }
    }
    
    public AccessControl getAccessControl(SecurityDomain domain)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAccessControl(domain.getId(), this.getId());
        }
    }
    
    // permissions handling
    
    /**
     * Does this contact have the given permission or does it 
     * inherit the permission from teams the contact is in
     * @param permission the permission to check for
     * @return true if this contact has the given permission
     */
    public Boolean checkPermission(Permission permission)
    {
        for (String granted : this.getGrantedPermissions())
        {
            if (permission.match(Permission.of(granted)))
                return true;
        }
        for (String revoked : this.getRevokedPermissions())
        {
            if (permission.match(Permission.of(revoked)))
                return false;
        }
        // go up the chain
        for (Team team : this.getTeams())
        {
            Boolean result = team.checkPermission(permission);
            if (result != null) return result;
        }
        return null;
    }
    
    public Boolean checkPermission(Permission permission, SecurityDomain domain)
    {
        // get the access controls for the given contact over the given domain
        AccessControl access = this.getAccessControl(domain);
        if (access != null)
        {
            for (String granted : access.getGrantedPermissions())
            {
                if (permission.match(Permission.of(granted)))
                    return true;
            }
            for (String revoked : access.getRevokedPermissions())
            {
                if (permission.match(Permission.of(revoked))) 
                    return false;
            }
        }
        // go up the chain
        for (Team team : this.getTeams())
        {
            Boolean result = team.checkPermission(permission, domain);
            if (result != null) return result;
        }
        return null;
    }
    
    public boolean hasPermission(Permission permission)
    {
        Boolean allowed = this.checkPermission(permission);
        return allowed == null ? false : allowed.booleanValue();
    }
    
    public boolean hasPermission(String permission)
    {
        return this.hasPermission(Permission.of(permission));
    }
    
    public boolean hasPermission(Permission permission, SecurityDomain domain)
    {
        Boolean allowed = this.checkPermission(permission);
        if (allowed != null) return allowed.booleanValue();
        allowed = this.checkPermission(permission, domain);
        return allowed == null ? false : allowed.booleanValue();
    }
    
    public boolean hasPermission(String permission, SecurityDomain domain)
    {
        return this.hasPermission(Permission.of(permission), domain);
    }
    
    public boolean hasPermission(Permission permission, Secured overObject)
    {
        Boolean allowed = this.checkPermission(permission);
        if (allowed) return allowed.booleanValue();
        for (SecurityDomain domain : overObject.getSecurityDomains())
        {
            allowed = this.checkPermission(permission, domain);
            if (allowed != null) return allowed.booleanValue();
        }
        return false;
    }
    
    public boolean hasPermission(String permission, Secured overObject)
    {
        return this.hasPermission(Permission.of(permission), overObject);
    }
    
    public <T extends Secured> List<T> hasPermission(Permission permission, Collection<T> overObjects)
    {
        List<T> filtered = new LinkedList<T>();
        for (T overObject : overObjects)
        {
            if (this.hasPermission(permission, overObject))
            {
                filtered.add(overObject);
            }
        }
        return filtered;
    }
    
    public <T extends Secured> Set<T> hasPermission(Permission permission, Set<T> overObjects)
    {
        Set<T> filtered = new HashSet<T>();
        for (T overObject : overObjects)
        {
            if (this.hasPermission(permission, overObject))
            {
                filtered.add(overObject);
            }
        }
        return filtered;
    }
    
    public <T extends Secured> List<T> hasPermission(String permission, Collection<T> overObjects)
    {
        return this.hasPermission(Permission.of(permission), overObjects);
    }
    
    public <T extends Secured> Set<T> hasPermission(String permission, Set<T> overObjects)
    {
        return this.hasPermission(Permission.of(permission), overObjects);
    }
    
    public <T extends Secured> List<T> hasPermission(Function<T,Permission> permission, Collection<T> overObjects)
    {
        List<T> filtered = new LinkedList<T>();
        for (T overObject : overObjects)
        {
            if (this.hasPermission(permission.apply(overObject), overObject))
            {
                filtered.add(overObject);
            }
        }
        return filtered;
    }
    
    public <T extends Secured> Set<T> hasPermission(Function<T, Permission> permission, Set<T> overObjects)
    {
        Set<T> filtered = new HashSet<T>();
        for (T overObject : overObjects)
        {
            if (this.hasPermission(permission.apply(overObject), overObject))
            {
                filtered.add(overObject);
            }
        }
        return filtered;
    }

    public String toString()
    {
        return "Contact {" + this.getName() + "}";
    }

    @Override
    public ContactMO toMO(boolean stub)
    {
        ContactMO mo = new ContactMO();
        super.toMO(mo, stub);
        mo.setEmail(this.getEmail());
        mo.setMobile(this.getMobile());
        mo.setPager(this.getPager());
        mo.setPhone(this.getPhone());
        mo.setGrantedPermissions(this.getGrantedPermissions());
        mo.setRevokedPermissions(this.getRevokedPermissions());
        if (!stub)
        {
            mo.setTeams(this.getTeams().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setNotifications(Util.nullable(this.getNotifications(), Notifications::toStubMO));
        }
        return mo;
    }
}

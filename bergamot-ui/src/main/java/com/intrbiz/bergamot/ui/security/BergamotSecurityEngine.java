package com.intrbiz.bergamot.ui.security;

import static com.intrbiz.balsa.BalsaContext.*;

import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.impl.security.SecurityEngineImpl;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.data.DataException;

public class BergamotSecurityEngine extends SecurityEngineImpl
{
    private Logger logger = Logger.getLogger(BergamotSecurityEngine.class);

    public BergamotSecurityEngine()
    {
        super();
    }

    @Override
    public String getEngineName()
    {
        return "Bergamot Security Engine";
    }
    
    protected String getMetricsIntelligenceSourceName()
    {
        return "com.intrbiz.bergamot";
    }

    @Override
    protected byte[] tokenForPrincipal(Principal principal)
    {
        byte[] token = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(token);
        UUID id = ((Contact) principal).getId();
        bb.putLong(id.getMostSignificantBits());
        bb.putLong(id.getLeastSignificantBits());
        return token;
    }

    @Override
    protected Principal principalForToken(byte[] token)
    {
        ByteBuffer bb = ByteBuffer.wrap(token);
        UUID id = new UUID(bb.getLong(), bb.getLong());
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getContact(id);
        }
    }

    @Override
    protected Principal doPasswordLogin(String username, char[] password) throws BalsaSecurityException
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            logger.info("Authentication for principal: " + username + ", server: " + Balsa().request().getServerName());
            logger.debug("Looking up site: " + Balsa().request().getServerName());
            // lookup the site
            Site site = db.getSiteByName(Balsa().request().getServerName());
            // validate
            if (site == null)
            {
                logger.error("Failed to determine the site for the server name: " + Balsa().request().getServerName() + ", authentication cannot continue.");
                this.invalidLogins.inc();
                throw new BalsaSecurityException("No such principal");
            }
            // lookup the principal
            Contact contact = db.getContactByNameOrEmail(site.getId(), username);
            // does the username exist?
            if (contact == null) return null;
            // check the password
            if (! contact.verifyPassword(new String(password)))
            {
                logger.error("Password mismatch for principal " + username + " => " + site.getId() + "::" + contact.getId());
                this.invalidLogins.inc();
                throw new BalsaSecurityException("Invalid password");
            }
            return contact;
        }
        catch (DataException e)
        {
            logger.error("Cannot authenticate principal, database error", e);
            this.validLogins.inc();
            throw new BalsaSecurityException("Error getting principal");
        }
    }

    /**
     * Check that the given principal has the given permission
     */
    @Override
    public boolean check(Principal principal, String permission)
    {
        if (principal instanceof Contact) 
            return this.check((Contact) principal, permission);
        return false;
    }
    
    protected boolean check(Contact contact, String permission)
    {
        for (String granted : contact.getGrantedPermissions())
        {
            if (this.matchPermission(granted, permission))
                return true;
        }
        for (String revoked : contact.getRevokedPermissions())
        {
            if (this.matchPermission(revoked, permission)) 
                return false;
        }
        // go up the chain
        for (Team team : contact.getTeams())
        {
            Boolean result = check(team, permission);
            if (result != null) return result;
        }
        return false;
    }
    
    /**
     * Tri-stated recursive checking of inherited permissions
     */
    protected Boolean check(Team team, String permission)
    {
        for (String granted : team.getGrantedPermissions())
        {
            if (this.matchPermission(granted, permission))
                return true;
        }
        for (String revoked : team.getRevokedPermissions())
        {
            if (this.matchPermission(revoked, permission)) 
                return false;
        }
        // go up the chain
        for (Team parent : team.getTeams())
        {
            Boolean result = check(parent, permission);
            if (result != null) return result;
        }
        return null;
    }
    
    /**
     * Match a granted or revoked permission against the requested permission
     * @param granted the permission pattern which has been granted of revoked
     * @param permission the permissions that is being checked for
     * @return true if they match
     */
    protected boolean matchPermission(String granted, String permission)
    {
        if (granted.equals(permission))
        {
            return true;
        }
        else if (granted.endsWith("*") && permission.startsWith(granted.substring(0, granted.length() - 1)))
        {
            return true;
        }
        return false;
    }
}

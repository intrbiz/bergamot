package com.intrbiz.bergamot.ui.security;

import static com.intrbiz.balsa.BalsaContext.*;

import java.nio.ByteBuffer;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.BalsaException;
import com.intrbiz.balsa.engine.impl.security.SecurityEngineImpl;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Permission;
import com.intrbiz.bergamot.model.SecuredObject;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.crypto.cookie.CookieBaker.Expires;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.data.DataException;

public class BergamotSecurityEngine extends SecurityEngineImpl
{
    private Logger logger = Logger.getLogger(BergamotSecurityEngine.class);
    
    private SecureRandom random = new SecureRandom();

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
    public void start() throws BalsaException
    {
    }
    
    @Override
    public boolean isValidPrincipal(Principal principal, ValidationLevel validationLevel)
    {
        if (validationLevel == ValidationLevel.STRONG)
            return principal instanceof Contact && (! (((Contact) principal).isForcePasswordChange() || ((Contact) principal).isLocked()));
        return principal instanceof Contact;
    }

    @Override
    protected byte[] tokenForPrincipal(Principal principal)
    {
        // we combine the UUID with a NONCE,
        // packed as: nonce1, nonce1 ^ msb, nonce2, nonce2 ^ lsb
        // this is a little obfuscation but mainly to reduce obvious 
        // patterns
        byte[] token = new byte[32];
        ByteBuffer bb = ByteBuffer.wrap(token);
        UUID id = ((Contact) principal).getId();
        // a NONCE
        long nonce1 = this.random.nextLong();
        long nonce2 = this.random.nextLong();
        // high part
        bb.putLong(nonce1);
        bb.putLong(id.getMostSignificantBits() ^ nonce1);
        // low part
        bb.putLong(nonce2);
        bb.putLong(id.getLeastSignificantBits() ^ nonce2);
        // done
        return token;
    }

    @Override
    protected Principal principalForToken(byte[] token)
    {
        ByteBuffer bb = ByteBuffer.wrap(token);
        // extract the data we need
        long nonce1 = bb.getLong();
        long msb    = bb.getLong();
        long nonce2 = bb.getLong();
        long lsb    = bb.getLong();
        // build the uuid
        UUID id = new UUID(nonce1 ^ msb, nonce2 ^ lsb);
        // lookup
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
                throw new BalsaSecurityException("Invalid password");
            }
            // check if the account is locked
            if (contact.isLocked())
            {
                logger.error("Rejecting valid login for principal " + username + " => " + site.getId() + " :: " + contact.getId() + " as the account has been locked.");
                throw new BalsaSecurityException("Account locked");
            }
            return contact;
        }
        catch (DataException e)
        {
            logger.error("Cannot authenticate principal, database error", e);
            throw new BalsaSecurityException("Error authenticating principal");
        }
    }
    
    @Override
    protected void validateAccessToken(String token, CryptoCookie cookie, Principal principal, CryptoCookie.Flag[] requiredFlags) throws BalsaSecurityException
    {
        // validate the flags
        if (requiredFlags != null)
        {
            for (CryptoCookie.Flag flag : requiredFlags)
            {
                if (! cookie.isFlagSet(flag)) throw new BalsaSecurityException("The flag: " + flag.mask + " is missing from the access token");
            }
        }
        // only validate perpetual API tokens
        if (cookie.getExpiryTime() == Expires.never())
        {
            // lookup the API token in the database and validate that it exists and is not revoked
            try (BergamotDB db = BergamotDB.connect())
            {
                APIToken apiToken = db.getAPIToken(token);
                if (apiToken == null)
                {
                    logger.error("Invalid perpetual API token '" + token + "', it does not exist");
                    throw new BalsaSecurityException("Invalid perpetual API token");
                }
                if ( ! ((Contact) principal).getId().equals(apiToken.getContactId()))
                {
                    logger.error("Invalid perpetual API token '" + token + "', it does not match the Principal");
                    throw new BalsaSecurityException("Invalid perpetual API token");
                }
                if (apiToken.isRevoked())
                {
                    logger.error("Invalid perpetual API token '" + token + "', it is revoked!");
                    throw new BalsaSecurityException("Invalid perpetual API token");
                }
            }
            catch (DataException e)
            {
                logger.error("Cannot authenticate perpetual API token, database error", e);
                throw new BalsaSecurityException("Failed to validate perpetual API token", e);
            }
        }
        // account level checks
        // check if the account is locked
        Contact contact = (Contact) principal;
        if (contact.isLocked())
        {
            logger.error("Rejecting valid token login for principal " + contact.getName() + " => " + contact.getSiteId() + " :: " + contact.getId() + " as the account has been locked.");
            throw new BalsaSecurityException("Account locked");
        }
    }

    /**
     * Check that the given principal has the given permission
     */
    @Override
    public boolean check(Principal principal, String permission)
    {
        if (principal instanceof Contact)
        {
            return ((Contact) principal).hasPermission(Permission.of(permission));
        }
        return false;
    }
    
    /**
     * Check that the given principal has permission over the given object or object UUID
     */
    @Override
    public boolean check(Principal principal, String permission, Object object)
    {
        if (principal instanceof Contact)
        {
            if (object instanceof SecuredObject)
            {
                return ((Contact) principal).hasPermission(Permission.of(permission), (SecuredObject<?,?>) object);
            }
            else if (object instanceof UUID)
            {
                return ((Contact) principal).hasPermission(Permission.of(permission), (UUID) object);
            }
        }
        return false;
    }    
}

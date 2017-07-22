package com.intrbiz.bergamot.ui.security;

import static com.intrbiz.balsa.BalsaContext.*;

import java.nio.ByteBuffer;
import java.security.Principal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.impl.security.BaseTwoFactorSecurityEngine;
import com.intrbiz.balsa.engine.impl.security.method.BackupCodeAuthenticationMethod;
import com.intrbiz.balsa.engine.impl.security.method.HOTPAuthenticationMethod;
import com.intrbiz.balsa.engine.impl.security.method.PasswordAuthenticationMethod;
import com.intrbiz.balsa.engine.impl.security.method.TokenAuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.security.BalsaPrincipalLockout;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Contact.LockOutReason;
import com.intrbiz.bergamot.model.ContactBackupCode;
import com.intrbiz.bergamot.model.ContactHOTPRegistration;
import com.intrbiz.bergamot.model.ContactU2FDeviceRegistration;
import com.intrbiz.bergamot.model.Permission;
import com.intrbiz.bergamot.model.SecuredObject;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.security.method.BergamotU2FAuthenticationMethod;
import com.intrbiz.crypto.cookie.CookieBaker.Expires;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.data.DataException;
import com.intrbiz.util.CounterHOTP.CounterHOTPState;
import com.intrbiz.util.HOTP.HOTPState;
import com.intrbiz.util.HOTPRegistration;
import com.yubico.u2f.data.DeviceRegistration;

public class BergamotSecurityEngine extends BaseTwoFactorSecurityEngine
{
    private static final int MAX_AUTH_FAILS = 10;
    
    private static final long AUTOMATIC_AUTH_LOCKOUT_PERIOD = TimeUnit.MINUTES.toMillis(15);
    
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

    /**
     * Override the authentication methods registered by default
     */
    @Override
    protected void setupDefaultAuthenticationMethods()
    {
        this.registerAuthenticationMethod(new PasswordAuthenticationMethod());
        this.registerAuthenticationMethod(new TokenAuthenticationMethod());
        // register 2FA authentication methods
        this.registerAuthenticationMethod(new BergamotU2FAuthenticationMethod());
        this.registerAuthenticationMethod(new HOTPAuthenticationMethod());
        this.registerAuthenticationMethod(new BackupCodeAuthenticationMethod());
    }

    @Override
    public boolean isTwoFactorAuthenticationRequiredForPrincipal(Principal principal)
    {
        return ((Contact) principal).isTwoFactorConfigured();
    }
    
    @Override
    public boolean isValidPrincipal(Principal principal, ValidationLevel validationLevel)
    {
        if (validationLevel == ValidationLevel.STRONG)
        {
            // validate that the principal is in a good state
            return principal instanceof Contact && (! ((Contact) principal).getSite().isDisabled()) && (! (((Contact) principal).isForcePasswordChange() || ((Contact) principal).isLocked()));
        }
        return principal instanceof Contact && (! ((Contact) principal).getSite().isDisabled());
    }

    @Override
    public byte[] tokenForPrincipal(Principal principal)
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
    public Principal principalForToken(byte[] token)
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
    public Principal doPasswordLogin(String username, char[] password) throws BalsaSecurityException
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
            // is the site disable
            if (site.isDisabled()) throw new BalsaSecurityException("Site is disabled");
            // lookup the principal
            Contact contact = db.getContactByNameOrEmail(site.getId(), username);
            // does the username exist?
            if (contact == null) return null;
            // is the account locked
            if (contact.isLocked())
            {
                // is this a temporary lockout
                if (contact.getLockedReason() == LockOutReason.AUTOMATIC)
                {
                    long timeDiff = System.currentTimeMillis() - contact.getLockedAt().getTime();
                    if (timeDiff > AUTOMATIC_AUTH_LOCKOUT_PERIOD)
                    {
                        logger.error("Unlocking principal " + username + " => " + site.getId() + " :: " + contact.getId());
                        contact.unlock();
                        db.setContact(contact);
                    }
                    else
                    {
                        logger.error("Rejecting login for principal " + username + " => " + site.getId() + " :: " + contact.getId() + " as the account has been locked.");
                        throw new BalsaPrincipalLockout();
                    }
                }
                else
                {
                    logger.error("Rejecting login for principal " + username + " => " + site.getId() + " :: " + contact.getId() + " as the account has been locked.");
                    throw new BalsaPrincipalLockout();
                }
            }
            // check the password
            if (! contact.verifyPassword(new String(password)))
            {
                logger.error("Password mismatch for principal " + username + " => " + site.getId() + "::" + contact.getId());
                // apply account lockout
                int authFails = Math.max(contact.getAuthFails(), 0) + 1;
                contact.setAuthFails(authFails);
                if (authFails >= MAX_AUTH_FAILS)
                {
                    // automatically lock this account out
                    logger.error("Automatically locking principal " + username + " => " + site.getId() + " :: " + contact.getId());
                    contact.lock(LockOutReason.AUTOMATIC);
                }
                db.setContact(contact);
                throw contact.isLocked() ? new BalsaPrincipalLockout() : new BalsaSecurityException("Invalid password");
            }
            // update authentication count
            contact.setAuthFails(0);
            contact.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            db.setContact(contact);
            // all good
            return contact;
        }
        catch (DataException e)
        {
            logger.error("Cannot authenticate principal, database error", e);
            throw new BalsaSecurityException("Error authenticating principal");
        }
    }
    
    @Override
    public void validateAccessToken(String token, CryptoCookie cookie, Principal principal, CryptoCookie.Flag[] requiredFlags) throws BalsaSecurityException
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
        // is the site disable
        if (contact.getSite().isDisabled()) throw new BalsaSecurityException("Site is disabled");
    }

    /**
     * Check that the given principal has the given permission
     */
    @Override
    public boolean check(Principal principal, String permission)
    {
        if (principal instanceof Contact)
        {
            Contact contact = (Contact) principal;
            return (! contact.getSite().isDisabled()) && contact.hasPermission(Permission.of(permission));
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
            Contact contact = (Contact) principal;
            if (contact.getSite().isDisabled()) return false;
            if (object instanceof SecuredObject)
            {
                return contact.hasPermission(Permission.of(permission), (SecuredObject<?,?>) object);
            }
            else if (object instanceof UUID)
            {
                return contact.hasPermission(Permission.of(permission), (UUID) object);
            }
        }
        return false;
    }

    @Override
    public void verifyBackupCode(Principal principal, String backupCode) throws BalsaSecurityException
    {
        ContactBackupCode contactBackupCode = ((Contact) principal).getBackupCodes().stream()
                                                    .filter((bc) -> bc.getCode().equals(backupCode)).findFirst().orElse(null);
        if (contactBackupCode == null || contactBackupCode.isUsed())
            throw new BalsaSecurityException("The given backup code is invalid");
    }

    @Override
    public void updateBackupCode(Principal principal, String backupCode) throws BalsaSecurityException
    {
        ContactBackupCode contactBackupCode = ((Contact) principal).getBackupCodes().stream()
                .filter((bc) -> bc.getCode().equals(backupCode)).findFirst().orElse(null);
        contactBackupCode.used();
        // update in the DB
        try (BergamotDB db = BergamotDB.connect())
        {
            db.setBackupCode(contactBackupCode);
        }
    }

    @Override
    public List<DeviceRegistration> getDeviceRegistrationsForPrincipal(Principal principal) throws BalsaSecurityException
    {
        return ((Contact) principal).getU2FDeviceRegistrations().stream()
                        .map(ContactU2FDeviceRegistration::toDeviceRegistration).collect(Collectors.toList());
    }

    @Override
    public String getAppIdForPrincipal(Principal principal) throws BalsaSecurityException
    {
        return ((Contact) principal).getSite().getU2FAppId();
    }

    @Override
    public void validateDeviceRegistration(Principal principal, DeviceRegistration device) throws BalsaSecurityException
    {
        // all validations are handled by U2F
    }

    @Override
    public void updateDeviceRegistration(Principal principal, DeviceRegistration device) throws BalsaSecurityException
    {
        ContactU2FDeviceRegistration authenticatedUsing = ((Contact) principal).getU2FDeviceRegistrations().stream()
                .filter((d) -> d.getKeyHandle().equals(device.getKeyHandle()) && d.getPublicKey().equals(device.getPublicKey()))
                .findFirst().get();
        // update the U2F state
        authenticatedUsing.used(device.getCounter());
        // update in the DB
        try (BergamotDB db = BergamotDB.connect())
        {
            db.setU2FDeviceRegistration(authenticatedUsing);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<HOTPRegistration> getHOTPRegistrationsForPrincipal(Principal principal) throws BalsaSecurityException
    {
        return (List<HOTPRegistration>) (List<?>) ((Contact) principal).getHOTPRegistrations();
    }

    @Override
    public void updateHOTPRegistration(Principal principal, HOTPRegistration registration, HOTPState nextState) throws BalsaSecurityException
    {   
        ContactHOTPRegistration chr = (ContactHOTPRegistration) registration;
        chr.used((CounterHOTPState) nextState);
        // update in the DB
        try (BergamotDB db = BergamotDB.connect())
        {
            db.setHOTPRegistration(chr);
        }
    }

    @Override
    public void validateHOTPRegistration(Principal principal, HOTPRegistration registration) throws BalsaSecurityException
    {
        if (((ContactHOTPRegistration) registration).isRevoked())
            throw new BalsaSecurityException("The HOTP registration is revoked");
    }
}
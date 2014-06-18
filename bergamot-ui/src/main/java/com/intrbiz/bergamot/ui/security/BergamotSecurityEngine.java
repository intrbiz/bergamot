package com.intrbiz.bergamot.ui.security;

import static com.intrbiz.balsa.BalsaContext.*;

import java.security.Principal;

import org.apache.log4j.Logger;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.intrbiz.balsa.engine.impl.security.SecurityEngineImpl;
import com.intrbiz.balsa.engine.security.Credentials;
import com.intrbiz.balsa.engine.security.PasswordCredentials;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.data.DataException;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;

public class BergamotSecurityEngine extends SecurityEngineImpl
{
    private Logger logger = Logger.getLogger(BergamotSecurityEngine.class);
    
    private final Timer authenticateTimer;
    
    private final Counter validLogins;
    
    private final Counter invalidLogins;

    public BergamotSecurityEngine()
    {
        super();
        // setup metrics
        IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot");
        this.authenticateTimer = source.getRegistry().timer(Witchcraft.name(BergamotSecurityEngine.class, "authenticate"));
        this.validLogins       = source.getRegistry().counter(Witchcraft.name(BergamotSecurityEngine.class, "valid-logins"));
        this.invalidLogins     = source.getRegistry().counter(Witchcraft.name(BergamotSecurityEngine.class, "invalid-logins"));
    }

    @Override
    public String getEngineName()
    {
        return "Bergamot Security Engine";
    }

    @Override
    public Principal authenticate(Credentials credentials) throws BalsaSecurityException
    {
        if (credentials instanceof PasswordCredentials)
        {
            Timer.Context tCtx = this.authenticateTimer.time();
            try
            {
                PasswordCredentials pw = (PasswordCredentials) credentials;
                try (BergamotDB db = BergamotDB.connect())
                {
                    logger.info("Authentication for principal: " + pw.getPrincipalName() + ", server: " + Balsa().request().getServerName());
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
                    Contact contact = db.getContactByNameOrEmail(site.getId(), pw.getPrincipalName());
                    // validate
                    if (contact == null)
                    {
                        logger.error("No such principal " + pw.getPrincipalName() + " for site " + site + " could be found.");
                        this.invalidLogins.inc();
                        throw new BalsaSecurityException("No such principal");
                    }
                    if (! contact.verifyPassword(new String(pw.getPassword())))
                    {
                        logger.error("Password mismatch for principal " + pw.getPrincipalName() + " => " + site + "::" + contact);
                        // TODO record login failure
                        this.invalidLogins.inc();
                        throw new BalsaSecurityException("Invalid password");
                    }
                    this.validLogins.inc();
                    return contact;
                }
                catch (DataException e)
                {
                    logger.error("Cannot authenticate principle, database error", e);
                    this.invalidLogins.inc();
                    throw new BalsaSecurityException("Error getting principal");
                }
            }
            finally
            {
                tCtx.stop();
            }
        }
        this.invalidLogins.inc();
        throw new BalsaSecurityException("No such principal");
    }
}

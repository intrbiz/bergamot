package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.GenericAuthenticationToken;
import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.BalsaValidationError;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.accounting.model.LoginAccountingEvent;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.U2FDeviceRegistration;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.bergamot.ui.security.password.check.BadPassword;
import com.intrbiz.bergamot.ui.security.password.check.PasswordCheckEngine;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsBoolean;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Cookie;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePrincipal;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;
import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.AuthenticateRequestData;
import com.yubico.u2f.data.messages.AuthenticateResponse;

@Prefix("/")
@Template("layout/single")
public class LoginRouter extends Router<BergamotApp>
{   
    private Logger logger = Logger.getLogger(LoginRouter.class);
    
    private Accounting accounting = Accounting.create(LoginRouter.class);
    
    private final U2F u2f = new U2F();
    
    @Get("/login")
    public void login(@Param("redirect") String redirect, @Cookie("bergamot.auto.login") String autoAuthToken) throws Exception
    {
        if (! Util.isEmpty(autoAuthToken))
        {
            // try the given auth token and assert the contact has ui.access permission
            Contact contact = tryAuthenticate(new GenericAuthenticationToken(autoAuthToken));
            if (contact != null && permission("ui.access"))
            {
                logger.info("Successfully auto authenticated user: " + contact.getName() + " => " + contact.getSiteId() + "::" + contact.getId());
                // accounting
                this.accounting.account(new LoginAccountingEvent(contact.getSiteId(), contact.getId(), request().getServerName(), null, balsa().session().id(), true, true, request().getRemoteAddress()));
                // setup the session
                sessionVar("contact", currentPrincipal());
                sessionVar("site", contact.getSite());
                // record the token in the session for removal on logout
                sessionVar("bergamot.auto.login", autoAuthToken);
                // now we can redirect
                if (contact.isForcePasswordChange())
                {
                    var("redirect", redirect);
                    var("forced", true);
                    encode("login/force_change_password");
                }
                else if (! contact.getU2FDeviceRegistrations().isEmpty())
                {
                    // start the U2F login
                    AuthenticateRequestData authData = this.u2f.startAuthentication(contact.getSite().getU2FAppId(), contact.getU2FDeviceRegistrations().stream().map(U2FDeviceRegistration::toDeviceRegistration).collect(Collectors.toList()));
                    // encode the U2F login view
                    var("redirect", redirect);
                    var("u2fauthenticate", authData);
                    encode("login/u2fauthenticate");
                }
                else
                {
                    // redirect
                    redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
                }
                return;
            }
        }
        // show the login page
        var("redirect", redirect);
        var("username", cookie("bergamot.username"));
        encode("login/login");
    }

    @Post("/login")
    @RequireValidAccessTokenForURL()
    @WithDataAdapter(BergamotDB.class)
    public void doLogin(BergamotDB db, @Param("username") String username, @Param("password") String password, @Param("redirect") String redirect, @Param("remember_me") @AsBoolean(defaultValue = false, coalesce = CoalesceMode.ALWAYS) Boolean rememberMe) throws Exception
    {
        logger.info("Login: " + username);
        authenticate(username, password);
        // assert that the contact is permitted UI access
        require(principal());
        require(permission("ui.access"));
        // store the current site and contact
        Contact contact = sessionVar("contact", currentPrincipal());
        sessionVar("site", contact.getSite());
        // account this login
        // accounting
        this.accounting.account(new LoginAccountingEvent(contact.getSiteId(), contact.getId(), request().getServerName(), username, balsa().session().id(), false, true, request().getRemoteAddress()));
        // set a cookie of the username, to remember the user
        cookie().name("bergamot.username").value(username).path(path("/login")).expiresAfter(90, TimeUnit.DAYS).httpOnly().set();
        // if remember me is selected then push a long term auth cookie
        if (rememberMe)
        {
            // generate the token
            String autoAuthToken = app().getSecurityEngine().generatePerpetualAuthenticationTokenForPrincipal(contact);
            // store the token
            db.setAPIToken(new APIToken(autoAuthToken, contact, "Auto login for " + request().getRemoteAddress()));
            // set the cookie
            cookie()
            .name("bergamot.auto.login")
            .value(autoAuthToken)
            .path(path("/login"))
            .expiresAfter(90, TimeUnit.DAYS)
            .httpOnly()
            .secure(request().isSecure())
            .set();
            // record the token in the session for removal on logout
            sessionVar("bergamot.auto.login", autoAuthToken);
        }
        // force a password change
        if (contact.isForcePasswordChange())
        {
            var("redirect", redirect);
            var("forced", true);
            encode("login/force_change_password");
        }
        else if (! contact.getU2FDeviceRegistrations().isEmpty())
        {
            // start the U2F login
            AuthenticateRequestData authData = this.u2f.startAuthentication(contact.getSite().getU2FAppId(), contact.getU2FDeviceRegistrations().stream().map(U2FDeviceRegistration::toDeviceRegistration).collect(Collectors.toList()));
            // encode the U2F login view
            var("redirect", redirect);
            var("u2fauthenticate", authData);
            encode("login/u2fauthenticate");
        }
        else
        {
            // require that the principal is strongly valid
            require(validPrincipal());
            // redirect
            redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
        }
    }
    
    @Post("/finish-u2f-authentication")
    @RequirePrincipal()
    @RequireValidAccessTokenForURL()
    @WithDataAdapter(BergamotDB.class)
    public void finishU2FAuthentication(BergamotDB db, @Param("u2f-authenticate-request") String u2fAuthenticateRequest, @Param("u2f-authenticate-response") String u2fAuthenticateResponse, @Param("redirect") String redirect) throws Exception
    {
        // the principal
        Contact contact = currentPrincipal();
        // parse the auth data
        List<U2FDeviceRegistration> u2fDevices = contact.getU2FDeviceRegistrations();
        AuthenticateRequestData req = AuthenticateRequestData.fromJson(u2fAuthenticateRequest);
        AuthenticateResponse resp = AuthenticateResponse.fromJson(u2fAuthenticateResponse);
        // process the authentication
        DeviceRegistration device = this.u2f.finishAuthentication(req, resp, u2fDevices.stream().map(U2FDeviceRegistration::toDeviceRegistration).collect(Collectors.toList()));
        // validate the registration counter
        U2FDeviceRegistration authenticatedUsing = u2fDevices.stream()
                                                    .filter((d) -> d.getKeyHandle().equals(device.getKeyHandle()) && d.getPublicKey().equals(device.getPublicKey()))
                                                    .findFirst().orElse(null);
        require(! authenticatedUsing.isRevoked(), "Your Security Key has been revoked!");
        require(authenticatedUsing.getCounter() < device.getCounter(), "Your Security Key counter is in the past!");
        // ensure we update the registration counter
        authenticatedUsing.setCounter(device.getCounter());
        authenticatedUsing.setUpdated(new Timestamp(System.currentTimeMillis()));
        db.setU2FDeviceRegistration(authenticatedUsing);;
        // done
        sessionVar("doneU2F", true);
        sessionVar("u2fDevice", device);
        // redirect
        redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
    }
    
    @Get("/change-password")
    @RequirePrincipal()
    public void changePassword(@Param("redirect") String redirect)
    {
        var("redirect", redirect);
        var("forced", false);
        encode("login/force_change_password");
    }
    
    @Post("/force-change-password")
    @RequirePrincipal()
    @RequireValidAccessTokenForURL()
    public void changePassword(@Param("password") @CheckStringLength(mandatory = true, min = 8) String password, @Param("confirm_password") @CheckStringLength(mandatory = true, min = 8) String confirmPassword, @Param("redirect") String redirect) throws IOException
    {
        try
        {
            // verify the password == confirm_password
            if (! password.equals(confirmPassword)) throw new BadPassword("mismatch");
            // enforce the default password policy
            PasswordCheckEngine.getDefaultInstance().check(password);
            // update the password
            Contact contact = currentPrincipal();
            logger.info("Processing password change for " + contact.getEmail() + " => " + contact.getSiteId() + "::" + contact.getId());
            try (BergamotDB db = BergamotDB.connect())
            {
                contact.hashPassword(password);
                db.setContact(contact);
            }
            // since we have updated the principal, we need to 
            // update it in the session
            balsa().session().currentPrincipal(contact);
            sessionVar("contact", contact);
            logger.info("Password change complete for " + contact.getEmail() + " => " + contact.getSiteId() + "::" + contact.getId());
            // redirect
            redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
        }
        catch (BadPassword e)
        {
            var("redirect", redirect);
            var("forced", true);
            var("error", e.getMessage());
            encode("login/force_change_password");
        }
    }
    
    @Catch(BalsaValidationError.class)
    @Catch(BalsaConversionError.class)
    @Order()
    @Post("/force-change-password")
    @RequirePrincipal()
    @RequireValidAccessTokenForURL()
    public void changePasswordError(@Param("redirect") String redirect) throws IOException
    {
        var("redirect", redirect);
        var("forced", true);
        var("error", "validation");
        encode("login/force_change_password");
    }

    @Get("/logout")
    @RequireValidPrincipal()
    @WithDataAdapter(BergamotDB.class)
    public void logout(BergamotDB db) throws IOException
    {
        // deauth the current session
        deauthenticate();
        // clean up any auto auth
        String autoAuthToken = sessionVar("bergamot.auto.login");
        if (! Util.isEmpty(autoAuthToken))
        {
            db.removeAPIToken(autoAuthToken);
            // nullify any auto auth cookie
            cookie()
            .name("bergamot.auto.login")
            .value("")
            .path(path("/login"))
            .expiresAfter(90, TimeUnit.DAYS)
            .httpOnly()
            .secure(request().isSecure())
            .set();
        }
        // redirect
        redirect("/login");
    }
    
    /**
     * Perform a password reset
     */
    @Get("/reset")
    public void reset(@Param("token") String token) throws IOException
    {
        // authenticate the token
        Contact contact = authenticate(new GenericAuthenticationToken(token, CryptoCookie.Flags.Reset));
        // assert that the contact requires a reset
        if (! contact.isForcePasswordChange())
        {
            // if the password has already been reset, then
            // this request is a little odd, so force a login
            redirect(path("/login"));
        }
        // setup the session
        logger.info("Successfully authenticated password reset for user: " + contact.getName() + " => " + contact.getSiteId() + "::" + contact.getId());
        // setup the session
        sessionVar("contact", currentPrincipal());
        sessionVar("site", contact.getSite());
        // force password change
        var("forced", true);
        encode("login/force_change_password");
    }
    
    @Catch(BalsaSecurityException.class)
    @Order()
    @Post("/login")
    public void loginError(@Param("username") String username, @Param("redirect") String redirect)
    {
        // error during login
        var("error", "invalid");
        var("redirect", redirect);
        var("username", cookie("bergamot.username"));
        // account this invalid login
        this.accounting.account(new LoginAccountingEvent(null, null, request().getServerName(), username, balsa().session().id(), false, false, request().getRemoteAddress()));
        // encode login page
        encode("login/login");
    }
    
    @Catch(BalsaSecurityException.class)
    @Order(Order.LAST - 10)
    @Any("/**")
    public void forceLogin(@Param("redirect") String redirect) throws IOException
    {
        String to = Util.isEmpty(redirect) ? request().getPathInfo() : redirect;
        redirect("/login?redirect=" + Util.urlEncode(to, Util.UTF8));
    }
    
    @Get("/reset-password")
    public void resetPassword(@Param("username") String username) throws IOException
    {
        var("username", Util.coalesceEmpty(username, cookie("bergamot.username"), null));
        encode("login/reset_password");
    }
    
    @Post("/reset-password")
    @RequireValidAccessTokenForURL()
    @WithDataAdapter(BergamotDB.class)
    public void doResetPassword(BergamotDB db, @Param("username") String username) throws IOException
    {
        // lookup the site
        Site site = db.getSiteByName(request().getServerName());
        if (site != null)
        {
            // lookup the contact
            Contact contact = db.getContactByNameOrEmail(site.getId(), username);
            if (contact != null)
            {
                action("reset-password", contact);
            }
            else
            {
                var("error", "no-such-contact");
                logger.info("Got password reset for a contact I don't know: '" + username + "'");
            }
        }
        else
        {
            var("error", "no-such-site");
            logger.info("Got password reset for a site I don't know: '" + request().getServerName() + "'");
        }
        encode("login/reset_password_sent");
    }
}

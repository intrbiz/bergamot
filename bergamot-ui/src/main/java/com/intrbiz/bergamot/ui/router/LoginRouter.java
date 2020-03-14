package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.AuthenticationResponse;
import com.intrbiz.balsa.engine.security.challenge.U2FAuthenticationChallenge;
import com.intrbiz.balsa.engine.security.credentials.BackupCodeCredentials;
import com.intrbiz.balsa.engine.security.credentials.GenericAuthenticationToken;
import com.intrbiz.balsa.engine.security.credentials.HOTPCredentials;
import com.intrbiz.balsa.engine.security.credentials.PasswordCredentials;
import com.intrbiz.balsa.engine.security.credentials.U2FAuthenticationChallengeResponse;
import com.intrbiz.balsa.engine.security.method.AuthenticationMethod;
import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.BalsaValidationError;
import com.intrbiz.balsa.error.http.BalsaBadRequest;
import com.intrbiz.balsa.error.security.BalsaPrincipalLockout;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.accounting.model.LoginAccountingEvent;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.GlobalSetting;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.bergamot.ui.security.password.check.BadPassword;
import com.intrbiz.bergamot.ui.security.password.check.PasswordCheckEngine;
import com.intrbiz.bergamot.ui.util.RecaptchaUtil;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsBoolean;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Cookie;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaInt;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireAuthenticating;
import com.intrbiz.metadata.RequirePrincipal;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/single")
public class LoginRouter extends Router<BergamotApp>
{   
    private Logger logger = Logger.getLogger(LoginRouter.class);
    
    private Accounting accounting = Accounting.create(LoginRouter.class);
    
    @Get("/login")
    @WithDataAdapter(BergamotDB.class)
    public void login(BergamotDB db, @Param("redirect") String redirect, @Cookie("bergamot.auto.login") String autoAuthToken) throws Exception
    {
        if (! Util.isEmpty(autoAuthToken))
        {
            // try the given auth token and assert the contact has ui.access permission
            AuthenticationResponse authResp = tryAuthenticate(new GenericAuthenticationToken(autoAuthToken));
            if (authResp != null)
            {
                // record the token in the session for removal on logout
                sessionVar("bergamot.auto.login", autoAuthToken);
                // complete the login
                this.completeLogin(authResp, redirect);
                return;
            }
        }
        // is this first install?
        GlobalSetting firstInstall = db.getGlobalSetting(GlobalSetting.NAME.FIRST_INSTALL);
        if (firstInstall == null)
        {
            // redirect to the first install helper
            redirect("/global/install/");
            return;
        }
        // show the login page
        var("redirect", redirect);
        var("username", cookie("bergamot.username"));
        encode("login/login");
    }

    @Post("/login")
    @RequireValidAccessTokenForURL()
    @WithDataAdapter(BergamotDB.class)
    public void doLogin(BergamotDB db, 
            @Param("username") String username, 
            @Param("password") String password, 
            @Param("redirect") String redirect, 
            @Param("remember_me") @AsBoolean(defaultValue = false, coalesce = CoalesceMode.ALWAYS) Boolean rememberMe, 
            @Param("g-recaptcha-response") String recaptchaResponse
    ) throws Exception
    {
        // get recaptcha settings
        Site site = var("site", db.getSiteByName(request().getServerName()));
        require(site != null, new BalsaBadRequest("Bad Site"));
        String recaptchaSiteKey = var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        String recaptchaSecretKey = site.getParameter("recaptcha-secret-key");
        // validate captcha
        if (! Util.isEmpty(recaptchaSiteKey))
        {
            // lookup the contact and assert if we require a captcha
            Contact theContact = db.getContactByNameOrEmail(site.getId(), username);
            require(theContact != null);
            // does this contact have any failed logins which forces a CAPTCHA
            if (theContact.getAuthFails() > 0)
            {
                // verify the captcha
                if (Util.isEmpty(recaptchaResponse))
                    throw new BalsaValidationError("reCAPTCHA response required");
                require(RecaptchaUtil.verify(request().getServerName(), recaptchaSecretKey, recaptchaResponse, null));
            }
        }
        // process login
        logger.info("Login: " + username);
        AuthenticationResponse authResp = authenticate(new PasswordCredentials.Simple(username, password));
        // set a cookie of the username, to remember the user
        cookie().name("bergamot.username").value(username).path(path("/login")).expiresAfter(90, TimeUnit.DAYS).httpOnly().set();
        // if remember me is selected then push a long term auth cookie
        if (rememberMe)
        {
            // get the contact which has authenticated
            Contact contact = authResp.getPrincipal();
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
        // complete the login
        this.completeLogin(authResp, redirect);
    }
    
    private void completeLogin(AuthenticationResponse authResp, String redirect) throws Exception
    {
        if (authResp.isComplete())
        {
            Contact contact = authResp.getPrincipal();
            // accounting
            this.accounting.account(new LoginAccountingEvent(contact.getSiteId(), contact.getId(), request().getServerName(), null, balsa().session().id(), false, true, request().getRemoteAddress()));
            // do we need to force a password change
            if (contact.isForcePasswordChange())
            {
                // setup recaptcha
                Site site = var("site", contact.getSite());
                var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
                var("redirect", redirect);
                var("forced", true);
                encode("login/force_change_password");
            }
            else
            {
                // redirect
                redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
            }
        }
        else
        {
            // trigger the two factor authentication
            U2FAuthenticationChallenge u2fChallenge = (U2FAuthenticationChallenge) authResp.getChallenges().get(AuthenticationMethod.U2F);
            if (u2fChallenge != null)
            {
                // encode the U2F login view
                var("redirect", redirect);
                var("u2fauthenticate", u2fChallenge.getChallenge());
                encode("login/u2f_authenticate");
            }
            else
            {
                this.startHOTPAuthentication(redirect);
            }
        }
    }
    
    @Any("/start-hotp-authentication")
    @RequireAuthenticating()
    public void startHOTPAuthentication(@Param("redirect") String redirect) throws Exception
    {
        // start the HOTP login
        var("redirect", redirect);
        encode("login/hotp_authenticate");
    }
    
    @Post("/finish-hotp-authentication")
    @RequireAuthenticating()
    /*@RequireValidAccessTokenForURL()*/
    @WithDataAdapter(BergamotDB.class)
    public void finishHOTPAuthentication(BergamotDB db, @Param("code") @IsaInt(min = 0, max = 999999, mandatory = true) int code, @Param("redirect") String redirect) throws Exception
    {
        AuthenticationResponse authResp = authenticate(new HOTPCredentials.Simple(code));
        this.completeLogin(authResp, redirect);
    }
    
    @Catch(BalsaValidationError.class)
    @Catch(BalsaConversionError.class)
    @Catch(BalsaSecurityException.class)
    @Post("/finish-hotp-authentication")
    @RequireAuthenticating()
    @RequireValidAccessTokenForURL()
    public void finishHOTPAuthenticationError(@Param("redirect") String redirect) throws Exception
    {
        // error during HOTP
        var("redirect", redirect);
        var("failed", true);
        encode("login/hotp_authenticate");
    }
    
    @Any("/start-backup-code-authentication")
    @RequireAuthenticating()
    public void startBackupCodeAuthentication() throws Exception
    {
        Contact contact = authenticationState().authenticatingPrincipal();
        // setup recaptcha
        Site site = var("site", contact.getSite());
        var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        encode("login/backup_code_authenticate");
    }
    
    @Post("/finish-backup-code-authentication")
    @RequireAuthenticating()
    @RequireValidAccessTokenForURL()
    @WithDataAdapter(BergamotDB.class)
    public void finishBackupCodeAuthentication(
            BergamotDB db, 
            @Param("code") String code,
            @Param("g-recaptcha-response") String recaptchaResponse
    ) throws Exception
    {
        Contact contact = authenticationState().authenticatingPrincipal();
        // get recaptcha settings
        Site site = contact.getSite();
        String recaptchaSiteKey = var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        String recaptchaSecretKey = site.getParameter("recaptcha-secret-key");
        // validate captcha
        if (! Util.isEmpty(recaptchaSiteKey))
        {
            if (Util.isEmpty(recaptchaResponse))
                throw new BalsaValidationError("reCAPTCHA response required");
            require(RecaptchaUtil.verify(request().getServerName(), recaptchaSecretKey, recaptchaResponse, null));
        }
        // process the 2nd factor authentication
        AuthenticationResponse authResp = authenticate(new BackupCodeCredentials.Simple(code));
        this.completeLogin(authResp, path("/profile/"));
    }
    
    @Catch(BalsaValidationError.class)
    @Catch(BalsaConversionError.class)
    @Catch(BalsaSecurityException.class)
    @Post("/finish-backup-code-authentication")
    @RequireAuthenticating()
    @RequireValidAccessTokenForURL()
    public void finishBackupCodeAuthenticationError(@Param("redirect") String redirect) throws Exception
    {
        Contact contact = authenticationState().authenticatingPrincipal();
        // setup recaptcha
        Site site = var("site", contact.getSite());
        var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        // error during backup code
        var("failed", true);
        encode("login/backup_code_authenticate");
    }
    
    @Post("/finish-u2f-authentication")
    @RequireAuthenticating()
    @RequireValidAccessTokenForURL()
    @WithDataAdapter(BergamotDB.class)
    public void finishU2FAuthentication(BergamotDB db, @Param("u2f-authenticate-request") String u2fAuthenticateRequest, @Param("u2f-authenticate-response") String u2fAuthenticateResponse, @Param("redirect") String redirect) throws Exception
    {
        AuthenticationResponse authResp = authenticate(new U2FAuthenticationChallengeResponse(u2fAuthenticateRequest, u2fAuthenticateResponse));
        this.completeLogin(authResp, redirect);
    }
    
    @Catch(BalsaValidationError.class)
    @Catch(BalsaConversionError.class)
    @Catch(BalsaSecurityException.class)
    @Post("/finish-u2f-authentication")
    @RequireAuthenticating()
    @RequireValidAccessTokenForURL()
    public void finishU2FAuthenticationError(@Param("redirect") String redirect) throws Exception
    {
        U2FAuthenticationChallenge u2fChallenge = (U2FAuthenticationChallenge) authenticationState().challenges().get(AuthenticationMethod.U2F);
        // encode the U2F login view
        var("failed", true);
        var("redirect", redirect);
        var("u2fauthenticate", u2fChallenge.getChallenge());
        encode("login/u2f_authenticate");
    }
    
    @Get("/change-password")
    @RequirePrincipal()
    public void changePassword(@Param("redirect") String redirect, @CurrentPrincipal Contact contact)
    {
        // setup recaptcha
        Site site = var("site", contact.getSite());
        var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        //
        var("redirect", redirect);
        var("forced", false);
        encode("login/force_change_password");
    }
    
    @Post("/force-change-password")
    @RequirePrincipal()
    @RequireValidAccessTokenForURL()
    public void changePassword(
            @Param("password") @CheckStringLength(mandatory = true, min = 8) String password, 
            @Param("confirm_password") @CheckStringLength(mandatory = true, min = 8) String confirmPassword, 
            @Param("redirect") String redirect,
            @Param("g-recaptcha-response") String recaptchaResponse
    ) throws IOException
    {
        // get recaptcha settings
        Contact contact = currentPrincipal();
        Site site = var("site", contact.getSite());
        require(site != null, new BalsaBadRequest("Bad Site"));
        String recaptchaSiteKey = var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        String recaptchaSecretKey = site.getParameter("recaptcha-secret-key");
        // validate captcha
        if (! Util.isEmpty(recaptchaSiteKey))
        {
            if (Util.isEmpty(recaptchaResponse))
                throw new BalsaValidationError("reCAPTCHA response required");
            require(RecaptchaUtil.verify(request().getServerName(), recaptchaSecretKey, recaptchaResponse, null));
        }
        // change the password
        try
        {
            // verify the password == confirm_password
            if (! password.equals(confirmPassword)) throw new BadPassword("mismatch");
            // enforce the default password policy
            PasswordCheckEngine.getDefaultInstance().check(password);
            // update the password
            logger.info("Processing password change for " + contact.getEmail() + " => " + contact.getSiteId() + "::" + contact.getId());
            try (BergamotDB db = BergamotDB.connect())
            {
                contact.hashPassword(password);
                db.setContact(contact);
            }
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
    @Catch(BalsaSecurityException.class)
    @Order()
    @Post("/force-change-password")
    @RequirePrincipal()
    @RequireValidAccessTokenForURL()
    public void changePasswordError(@Param("redirect") String redirect) throws IOException
    {
        var("redirect", redirect);
        var("forced", true);
        var("error", "validation");
        // setup the recaptcha
        Contact contact = var("contact", currentPrincipal());
        Site site = var("site", contact.getSite());
        var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        //
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
        Contact contact = authenticateSingleFactor(new GenericAuthenticationToken(token, CryptoCookie.Flags.Reset), true);
        // assert that the contact requires a reset
        if (! contact.isForcePasswordChange())
        {
            // if the password has already been reset, then
            // this request is a little odd, so force a login
            redirect(path("/login"));
        }
        // setup the session
        logger.info("Successfully authenticated password reset for user: " + contact.getName() + " => " + contact.getSiteId() + "::" + contact.getId());
        // setup the recaptcha
        var("contact", currentPrincipal());
        Site site = var("site", contact.getSite());
        var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        // force password change
        var("forced", true);
        encode("login/force_change_password");
    }
    
    @Catch(BalsaPrincipalLockout.class)
    @Order(-10)
    @Any("/**")
    public void lockoutError()
    {
        encode("login/locked");
    }
    
    @Catch(BalsaSecurityException.class)
    @Order(10)
    @Post("/login")
    @WithDataAdapter(BergamotDB.class)
    public void loginError(BergamotDB db, @Param("username") String username, @Param("redirect") String redirect)
    {
        // error during login
        var("error", "invalid");
        var("redirect", redirect);
        var("username", cookie("bergamot.username"));
        // setup recaptcha
        Site site = var("site", db.getSiteByName(request().getServerName()));
        require(site != null, new BalsaBadRequest("Bad Site"));
        var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
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
    @WithDataAdapter(BergamotDB.class)
    public void resetPassword(BergamotDB db, @Param("username") String username) throws IOException
    {
        // setup recaptcha
        Site site = var("site", db.getSiteByName(request().getServerName()));
        require(site != null, new BalsaBadRequest("Bad Site"));
        var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
        // be nice about username
        var("username", Util.coalesceEmpty(username, cookie("bergamot.username"), null));
        encode("login/reset_password");
    }
    
    @Post("/reset-password")
    @RequireValidAccessTokenForURL()
    @WithDataAdapter(BergamotDB.class)
    public void doResetPassword(
            BergamotDB db, 
            @Param("username") String username,
            @Param("g-recaptcha-response") String recaptchaResponse
    ) throws IOException
    {
        // lookup the site
        Site site = db.getSiteByName(request().getServerName());
        if (site != null)
        {   
            // get recaptcha settings
            require(site != null, new BalsaBadRequest("Bad Site"));
            String recaptchaSiteKey = var("recaptchaSiteKey", site.getParameter("recaptcha-site-key"));
            String recaptchaSecretKey = site.getParameter("recaptcha-secret-key");
            // validate captcha
            if (! Util.isEmpty(recaptchaSiteKey))
            {
                if (Util.isEmpty(recaptchaResponse))
                    throw new BalsaValidationError("reCAPTCHA response required");
                require(RecaptchaUtil.verify(request().getServerName(), recaptchaSecretKey, recaptchaResponse, null));
            }
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

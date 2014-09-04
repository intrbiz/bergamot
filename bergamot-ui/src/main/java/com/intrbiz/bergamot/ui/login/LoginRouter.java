package com.intrbiz.bergamot.ui.login;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.BalsaValidationError;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePrincipal;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidPrincipal;

@Prefix("/")
public class LoginRouter extends Router<BergamotApp>
{
    public static final String USERNAME_COOKIE = "bergamot.username";
    
    private Logger logger = Logger.getLogger(LoginRouter.class);
    
    @Get("/login")
    public void login(@Param("redirect") String redirect)
    {
        model("redirect", redirect);
        model("username", cookie(USERNAME_COOKIE));
        encodeOnly("login/login");
    }

    @Post("/login")
    @RequireValidAccessTokenForURL()
    public void doLogin(@Param("username") String username, @Param("password") String password, @Param("redirect") String redirect) throws IOException
    {
        logger.info("Login: " + username);
        authenticate(username, password);
        // assert that the contact is permitted UI access
        require(permission("ui.access"));
        // store the current site and contact
        Contact contact = sessionVar("contact", currentPrincipal());
        sessionVar("site", contact.getSite());
        // set a cookie of the username, to remember the user
        cookie().name(USERNAME_COOKIE).value(username).path(path("/login")).expiresAfter(90, TimeUnit.DAYS).httpOnly().set();
        // force a password change
        if (contact.isForcePasswordChange())
        {
            var("redirect", redirect);
            var("forced", true);
            encodeOnly("login/force_change_password");
        }
        else
        {
            // redirect
            redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
        }
    }
    
    @Post("/force-change-password")
    @RequirePrincipal()
    @RequireValidAccessTokenForURL()
    public void changePassword(@Param("password") @CheckStringLength(mandatory = true, min = 8) String password, @Param("confirm_password") @CheckStringLength(mandatory = true, min = 8) String confirmPassword, @Param("redirect") String redirect) throws IOException
    {
        if (password.equals(confirmPassword))
        {
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
            logger.info("Password change complete for " + contact.getEmail() + " => " + contact.getSiteId() + "::" + contact.getId());
            // redirect
            redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
        }
        else
        {
            var("redirect", redirect);
            var("forced", true);
            var("error", "mismatch");
            encodeOnly("login/force_change_password");
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
        encodeOnly("login/force_change_password");
    }

    @Get("/logout")
    @RequireValidPrincipal()
    public void logout() throws IOException
    {
        deauthenticate();
        redirect("/login");
    }
    
    @Catch(BalsaSecurityException.class)
    @Order()
    @Post("/login")
    public void loginError(@Param("username") String username, @Param("redirect") String redirect)
    {
        // error during login
        var("error", "invalid");
        var("redirect", redirect);
        var("username", cookie(USERNAME_COOKIE));
        // encode login page
        encodeOnly("login/login");
    }
    
    @Catch(BalsaSecurityException.class)
    @Order(Order.LAST)
    @Any("/**")
    public void forceLogin(@Param("redirect") String redirect) throws IOException
    {
        String to = Util.isEmpty(redirect) ? request().getPathInfo() : redirect;
        redirect("/login?redirect=" + Util.urlEncode(to, Util.UTF8));
    }
}

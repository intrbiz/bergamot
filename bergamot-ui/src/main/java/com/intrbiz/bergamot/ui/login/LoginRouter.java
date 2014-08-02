package com.intrbiz.bergamot.ui.login;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;

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
        // redirect
        redirect(Util.isEmpty(redirect) ? "/" : path(redirect));
    }

    @Get("/logout")
    public void logout() throws IOException
    {
        deauthenticate();
        redirect("/login");
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

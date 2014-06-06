package com.intrbiz.bergamot.ui.login;

import java.io.IOException;

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
    @Get("/login")
    public void login(@Param("redirect") String redirect)
    {
        model("redirect", redirect);
        encodeOnly("login/login");
    }

    @Post("/login")
    @RequireValidAccessTokenForURL()
    public void doLogin(@Param("username") String username, @Param("password") String password, @Param("redirect") String redirect) throws IOException
    {
        System.out.println("Login: " + username);
        authenticate(username, password);
        // store the current site and contact
        Contact contact = sessionVar("contact", currentPrincipal());
        sessionVar("site", contact.getSite());
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

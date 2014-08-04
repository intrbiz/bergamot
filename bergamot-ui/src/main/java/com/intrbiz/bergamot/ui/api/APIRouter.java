package com.intrbiz.bergamot.ui.api;

import java.util.concurrent.TimeUnit;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.GenericAuthenticationToken;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.ErrorMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.IgnorePaths;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;

@Prefix("/api/")
public class APIRouter extends Router<BergamotApp>
{   
    /**
     * Default global API 404 error handler
     */
    @Catch(BalsaNotFound.class)
    @Any("**")
    @JSON(status = HTTPStatus.NotFound)
    public ErrorMO notFound()
    {
        return new ErrorMO("Not found");
    }
    
    /**
     * Default global API 403 error handler
     */
    @Catch(BalsaSecurityException.class)
    @Any("**")
    @JSON(status = HTTPStatus.Forbidden)
    public ErrorMO accessDenied()
    {
        return new ErrorMO("Access denied");
    }
    
    /**
     * Authenticate an API Request based on the authentication 
     * token given on the request (if any). 
     */
    @Before
    @Any("**")
    /* We don't want to filter the authentication routes */
    @IgnorePaths({"/auth-token", "/extend-auth-token"})
    @Order(10)
    @WithDataAdapter(BergamotDB.class)
    public void authenticateRequest(BergamotDB db)
    {
        // perform a token based request authentication
        // we may already have the auth from the session, if shared with a UI session
        if (! this.validPrincipal())
        {
            authenticateRequest(new GenericAuthenticationToken(Util.coalesceEmpty(cookie("bergamot.api.key"), param("key"))));
        }
        // assert that the contact is permitted API access
        require(permission("api.access"));
        // setup the site based on the authenticated principal
        Contact contact = var("contact", currentPrincipal());
        var("site", contact.getSite());
    }
    
    /**
     * Authenticate a user for API access, 
     */
    @Any("/auth-token")
    @JSON()
    public AuthTokenMO getAuthToken(@Param("username") String username, @Param("password") String password)
    {
        authenticateRequest(username, password);
        long expiresAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        String token = app().getSecurityEngine().generateAuthenticationTokenForPrincipal(currentPrincipal(), expiresAt);
        return new AuthTokenMO(token, expiresAt);
    }
    
    /**
     * Extend an authentication token 
     */
    @Any("/extend-auth-token")
    @JSON()
    public AuthTokenMO extendAuthToken(@Param("auth-token") String token)
    {
        authenticateRequest(new GenericAuthenticationToken(token));
        long expiresAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        String newToken = app().getSecurityEngine().generateAuthenticationTokenForPrincipal(currentPrincipal(), expiresAt);
        return new AuthTokenMO(newToken, expiresAt);
    }
}

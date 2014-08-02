package com.intrbiz.bergamot.ui.api;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.GenericAuthenticationToken;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.ErrorMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Order;
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
}

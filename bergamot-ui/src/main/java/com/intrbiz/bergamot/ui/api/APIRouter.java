package com.intrbiz.bergamot.ui.api;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.GenericAuthenticationToken;
import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.BalsaValidationError;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.ErrorMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.converter.ConversionException;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.IgnorePaths;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.XML;
import com.intrbiz.validator.ValidationException;

@Prefix("/api/")
public class APIRouter extends Router<BergamotApp>
{   
    private Logger logger = Logger.getLogger(APIRouter.class);
    
    /**
     * Default global API 404 error handler for XML responses (config)
     */
    @Catch(BalsaNotFound.class)
    @Any("**\\.xml")
    @Order(10)
    @XML(status = HTTPStatus.NotFound)
    public ErrorMO notFoundXML()
    {
        return new ErrorMO("Not found");
    }
    
    /**
     * Default global API 403 error handler for XML responses (config)
     */
    @Catch(BalsaSecurityException.class)
    @Any("**\\.xml")
    @Order(10)
    @XML(status = HTTPStatus.Forbidden)
    public ErrorMO accessDeniedXML()
    {
        return new ErrorMO("Access denied");
    }
    
    /**
     * Default global API 404 error handler
     */
    @Catch(BalsaNotFound.class)
    @Any("**")
    @Order(20)
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
    @Order(20)
    @JSON(status = HTTPStatus.Forbidden)
    public ErrorMO accessDenied()
    {
        return new ErrorMO("Access denied");
    }
    
    /**
     * Validation and Conversion error handler
     */
    @Catch({ BalsaValidationError.class, BalsaConversionError.class })
    @Any("**")
    @Order(30)
    @JSON(status = HTTPStatus.BadRequest)
    public ErrorMO invalideRequest()
    {
        for (ConversionException cex : balsa().getConversionErrors())
        {
            logger.error("Conversion exception on request", cex);
        }
        for (ValidationException vex : balsa().getValidationErrors())
        {
            logger.error("Validation exception on request", vex);
        }
        return new ErrorMO("Bad Request");
    }
    
    /**
     * Default global API 500 error handler
     */
    @Catch()
    @Any("**")
    @Order(Order.LAST)
    @JSON(status = HTTPStatus.InternalServerError)
    public ErrorMO internalServerError()
    {
        Throwable error = balsa().getException();
        if (error != null)
        {
            logger.error("Caught internal server error: " + error.getMessage(), error);
        }
        return new ErrorMO(error == null || Util.isEmpty(error.getMessage()) ? "Not sure what happened here!" : error.getMessage());
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

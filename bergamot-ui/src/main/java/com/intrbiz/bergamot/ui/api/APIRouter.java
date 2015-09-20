package com.intrbiz.bergamot.ui.api;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.GenericAuthenticationToken;
import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.BalsaValidationError;
import com.intrbiz.balsa.error.http.BalsaBadRequest;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.converter.ConversionException;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.IgnorePaths;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
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
    public APIError notFoundXML()
    {
        return new APIError("Not found");
    }
    
    /**
     * Default global API 403 error handler for XML responses (config)
     */
    @Catch(BalsaSecurityException.class)
    @Any("**\\.xml")
    @Order(10)
    @XML(status = HTTPStatus.Forbidden)
    public APIError accessDeniedXML()
    {
        return new APIError("Access denied");
    }
    
    /**
     * Default global API 404 error handler
     */
    @Catch(BalsaNotFound.class)
    @Any("**")
    @Order(20)
    @JSON(status = HTTPStatus.NotFound)
    public APIError notFound()
    {
        return new APIError("Not found");
    }
    
    /**
     * Default global API 403 error handler
     */
    @Catch(BalsaSecurityException.class)
    @Any("**")
    @Order(20)
    @JSON(status = HTTPStatus.Forbidden)
    public APIError accessDenied()
    {
        return new APIError("Access denied");
    }
    
    /**
     * Validation and Conversion error handler
     */
    @Catch({ BalsaValidationError.class, BalsaConversionError.class })
    @Any("**")
    @Order(30)
    @JSON(status = HTTPStatus.BadRequest)
    public APIError invalideRequest()
    {
        for (ConversionException cex : balsa().getConversionErrors())
        {
            logger.error("Conversion exception on request", cex);
        }
        for (ValidationException vex : balsa().getValidationErrors())
        {
            logger.error("Validation exception on request", vex);
        }
        return new APIError("Bad Request");
    }
    
    /**
     * Validation and Conversion error handler
     */
    @Catch(BalsaBadRequest.class)
    @Any("**")
    @Order(40)
    @JSON(status = HTTPStatus.BadRequest)
    public APIError badRequest()
    {
        Throwable error = balsa().getException();
        if (error != null)
        {
            logger.error("Caught internal bad request error: " + error.getMessage(), error);
        }
        return new APIError("Bad Request: " + (error == null || Util.isEmpty(error.getMessage()) ? "Not sure what happened here!" : error.getMessage()));
    }
    
    /**
     * Default global API 500 error handler
     */
    @Catch()
    @Any("**")
    @Order(Order.LAST)
    @JSON(status = HTTPStatus.InternalServerError)
    public APIError internalServerError()
    {
        Throwable error = balsa().getException();
        if (error != null)
        {
            logger.error("Caught internal server error: " + error.getMessage(), error);
        }
        return new APIError(error == null || Util.isEmpty(error.getMessage()) ? "Not sure what happened here!" : error.getMessage());
    }
    
    /**
     * Authenticate an API Request based on the authentication 
     * token given on the request (if any). 
     */
    @Before
    @Any("**")
    /* We don't want to filter the authentication routes */
    @IgnorePaths({"/auth-token", "/extend-auth-token", "/test/hello/world", "/app/auth-token"})
    @Order(10)
    @WithDataAdapter(BergamotDB.class)
    public void authenticateRequest(BergamotDB db)
    {
        // perform a token based request authentication
        // we may already have the auth from the session, if shared with a UI session
        if (! this.validPrincipal())
        {
            authenticateRequest(new GenericAuthenticationToken(Util.coalesceEmpty(header("X-Bergamot-Auth"), cookie("bergamot.api.key"), param("key"))));
        }
        // assert that the contact is permitted API access
        require(permission("api.access"));
        // setup the site based on the authenticated principal
        Contact contact = var("contact", currentPrincipal());
        var("site", contact.getSite());
    }
    
    /**
     * Authenticate a user for API access on behalf of an application, 
     * this will generate a perpetual auth token
     */
    @Any("/app/auth-token")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public AuthTokenMO getAppAuthToken(BergamotDB db, @Param("app") @CheckStringLength(mandatory = true, min = 3, max = 80) String appName, @Param("username") String username, @Param("password") String password)
    {
        authenticateRequest(username, password);
        String token = app().getSecurityEngine().generatePerpetualAuthenticationTokenForPrincipal(currentPrincipal());
        db.setAPIToken(new APIToken(token, currentPrincipal(), Util.coalesceEmpty("Application: " + appName)));
        return new AuthTokenMO(token, 0L);
    }
    
    /**
     * Authenticate a user for API access, 
     */
    @Any("/auth-token")
    @JSON()
    @IgnoreBinding
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
    
    /**
     * Change the current users password
     */
    @Any("/change-password")
    @JSON()
    @RequireValidPrincipal()
    @WithDataAdapter(BergamotDB.class)
    public Boolean changePassword(
            BergamotDB db, 
            @Param("current-password") @CheckStringLength(min = 1, max = 80, mandatory = true) String currentPassword,
            @Param("new-password")     @CheckStringLength(min = 1, max = 80, mandatory = true) String newPassword
    )
    {
        Contact contact = currentPrincipal();
        // verify the given current password before changing the password
        if (! contact.verifyPassword(currentPassword)) throw new BalsaSecurityException("Failed to verify current password");
        // change the password
        contact.hashPassword(newPassword);
        // update the contact
        db.setContact(contact);
        return true;
    }
}

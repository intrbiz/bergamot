package com.intrbiz.bergamot.ui.api;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.engine.security.credentials.GenericAuthenticationToken;
import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.balsa.error.BalsaValidationError;
import com.intrbiz.balsa.error.http.BalsaBadRequest;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.AuthTokenMO;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.converter.ConversionException;
import com.intrbiz.crypto.cookie.CryptoCookie;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IgnoreMethods;
import com.intrbiz.metadata.IgnorePaths;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Options;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.XML;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;
import com.intrbiz.validator.ValidationException;

@Title("Authentication API Methods")
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
     * CORS Preflight Handling
     */
    @Options("**")
    @Order(-10)
    @IgnoreBinding
    public void handleCORSPreflight()
    {
        response()
         .header("Access-Control-Allow-Origin", "*")
         .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
         .header("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Bergamot-Auth")
         .header("Access-Control-Max-Age", " 86400")
         .plain()
         .ok();
    }
    
    /**
     * Inject CORS response headers
     */
    @Before
    @Any("**")
    @IgnoreMethods({"OPTIONS"})
    @Order(5)
    public void addCORSHeaders()
    {
        response()
        .header("Access-Control-Allow-Origin", "*");
    }
    
    /**
     * Authenticate an API Request based on the authentication 
     * token given on the request (if any). 
     */
    @Before
    @Any("**")
    /* We don't want to filter the certain routes */
    @IgnorePaths({ "/test/hello/world", "/auth-token" })
    @Order(10)
    @WithDataAdapter(BergamotDB.class)
    public void authenticateRequest(BergamotDB db)
    {
        // perform a token based request authentication
        // we may already have the auth from the session, if shared with a UI session
        if (! this.validPrincipal())
        {
            authenticateRequestSingleFactor(new GenericAuthenticationToken(Util.coalesceEmpty(header("Authorization"), header("X-Bergamot-Auth"), cookie("bergamot.api.key"), param("key")), CryptoCookie.Flags.Principal));
        }
        // assert that the contact is permitted API access
        require(permission("api.access"));
        // setup the site based on the authenticated principal
        Contact contact = var("contact", currentPrincipal());
        var("site", contact.getSite());
    }
    
    /**
     * Generate a short-lived authentication token which represents the currently authenticated principal for 1 hour.
     * 
     * Note: this can only be called with a perpetual access token, or via a valid UI session
     */
    @Title("Generate temporary authentication token")
    @Desc({
        "Temporary authentication tokens last for 1 hour from creation and can be used to authorize subsequent requests to the Bergamot Monitoring API wit the same level of access as requestor."
    })
    @Get("/auth-token")
    @JSON()
    public AuthTokenMO getAuthToken()
    {
        // perform a token based request authentication
        // we may already have the auth from the session, if shared with a UI session
        if (! this.validPrincipal())
        {
            authenticateRequestSingleFactor(new GenericAuthenticationToken(Util.coalesceEmpty(header("Authorization"), header("X-Bergamot-Auth"), cookie("bergamot.api.key"), param("key")), CryptoCookie.Flags.Perpetual, CryptoCookie.Flags.Principal));
        }
        // assert that the contact is permitted API access
        require(permission("api.access"));
        // generate temporary access token
        long expiry = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        String token = app().getSecurityEngine().generateAuthenticationTokenForPrincipal(currentPrincipal(), expiry, CryptoCookie.Flags.Principal);
        return new AuthTokenMO(token, expiry);
    }
}

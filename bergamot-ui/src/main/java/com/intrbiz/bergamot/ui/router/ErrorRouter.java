package com.intrbiz.bergamot.ui.router;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.BalsaConversionError;
import com.intrbiz.balsa.error.BalsaValidationError;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.Order;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Status;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/single")
public class ErrorRouter extends Router<BergamotApp>
{   
    private Logger logger = Logger.getLogger(ErrorRouter.class);
    
    @Any("/test/error")
    @Order(Order.LAST)
    public void errorTest() throws Exception
    {
        throw new RuntimeException("Testing 1 2 3 ...");
    }
    
    @Catch(BalsaNotFound.class)
    @Any("**")
    @Order(Order.LAST)
    public void notFoundError() throws IOException
    {
        logger.warn("Not found: " + balsa().request().getPathInfo());
        redirect("/");
    }
    
    @Catch({ BalsaValidationError.class, BalsaConversionError.class })
    @Any("**")
    @Order(Order.LAST)
    @Status(HTTPStatus.BadRequest)
    public void badRequest()
    {
        logger.error("Bad request: " + balsa().getValidationErrors());
        encode("error/400");
    }
    
    @Catch()
    @Any("**")
    @Order(Order.LAST)
    @Status(HTTPStatus.InternalServerError)
    public void unhandledError()
    {
        logger.error("Unhandle error handling request", balsa().getException());
        encode("error/500");
    }
}

package com.intrbiz.bergamot.ui.api;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Catch;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;

@Prefix("/api/")
public class APIRouter extends Router<BergamotApp>
{
    
    @Catch(BalsaNotFound.class)
    @Any("**")
    @JSON(status = HTTPStatus.NotFound)
    public String notFound()
    {
        return "Not found";
    }
}

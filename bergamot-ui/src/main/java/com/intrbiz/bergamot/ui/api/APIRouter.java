package com.intrbiz.bergamot.ui.api;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.http.HTTP.HTTPStatus;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
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
    
    @Any("**")
    @Before
    @WithDataAdapter(BergamotDB.class)
    public void lookupSite(BergamotDB db)
    {
        // we want to avoid a session for the API,
        // so lookup the site
        Site site = var("site", db.getSiteByName(request().getServerName()));
        if (site == null) throw new BalsaNotFound("No Bergamot site is configured for the server name: " + request().getServerName());
    }
}

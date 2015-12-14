package com.intrbiz.bergamot.ui.api;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaInt;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;

@Prefix("/api/util")
@RequireValidPrincipal()
public class UtilAPIRouter extends Router<BergamotApp>
{   
    @Get("/version")
    @JSON()
    public String version()
    {
        return BergamotApp.VERSION.NUMBER + " (" + BergamotApp.VERSION.CODE_NAME + ")";
    }
    
    @Get("/version/number")
    @JSON()
    public String versionNumber()
    {
        return BergamotApp.VERSION.NUMBER;
    }
    
    @Get("/version/codename")
    @JSON()
    public String versionCodeName()
    {
        return BergamotApp.VERSION.CODE_NAME;
    }
    
    @Get("/id/new")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public UUID newId(@Var("site") Site site)
    {
        return site.randomObjectId();
    }
    
    @Get("/id/new/:count")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(UUID.class)
    public List<UUID> newIds(@Var("site") Site site, @IsaInt(min = 1, max = 1000) Integer count)
    {
        List<UUID> ids = new LinkedList<UUID>();
        for (int i = 0; i < count; i++)
        {
            ids.add(site.randomObjectId());
        }
        return ids;
    }
}

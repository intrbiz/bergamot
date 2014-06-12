package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Var;


@Prefix("/api/trap")
public class TrapAPIRouter extends Router<BergamotApp>
{    
    @Get("/name/:host/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TrapMO getTrap(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {    
        return Util.nullable(db.getTrapOnHostByName(site.getId(), hostName, name), Trap::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TrapMO getTrap(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTrap(id), Trap::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getTrapState(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTrap(id), (t)->{return t.getState().toMO();});
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getTrapState(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {    
        return Util.nullable(db.getTrapOnHostByName(site.getId(), hostName, name), (t)->{return t.getState().toMO();});
    }
}

package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/trap")
public class TrapAPIRouter extends Router<BergamotApp>
{    
    @Get("/name/:host/:name")
    @JSON(notFoundIfNull = true)
    public TrapMO getTrap(String hostName, String name)
    {    
        return Util.nullable((Trap)Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(hostName), (h)->{return h.getTrap(name);}), Trap::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public TrapMO getTrap(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTrap(id), Trap::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getTrapState(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTrap(id), (t)->{return t.getState().toMO();});
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    public CheckStateMO getTrapState(String hostName, String name)
    {    
        return Util.nullable((Trap)Util.nullable(this.app().getBergamot().getObjectStore().lookupHost(hostName), (h)->{return h.getTrap(name);}), (t)->{return t.getState().toMO();});
    }
}

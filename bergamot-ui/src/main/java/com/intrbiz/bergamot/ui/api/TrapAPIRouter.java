package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
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
    
    @Any("/id/:id/submit")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public String getServiceState(BergamotDB db, @AsUUID() UUID id, @Param("status") String status, @Param("output") String output)
    {   
        Trap trap = db.getTrap(id);
        if (trap == null) return null;
        // the result
        Result result = new Result();
        result.setId(UUID.randomUUID());
        result.setCheckType(trap.getType());
        result.setCheckId(trap.getId());
        result.setSiteId(trap.getSiteId());
        result.setProcessingPool(trap.getPool());
        result.setStatus(status);
        result.setOk("OK".equalsIgnoreCase(status));
        result.setOutput(output);
        result.setExecuted(0);
        result.setRuntime(0);
        result.setParameter("bergamot.ui.instance", this.app().getInstanceName());
        // dispatch the result for processing
        action("dispatch-result", result);
        return "Accepted";
    }
}

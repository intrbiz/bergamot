package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.result.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/trap")
@RequireValidPrincipal()
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
    public TrapMO getTrap(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getTrap(id), Trap::toMO);
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getTrapState(BergamotDB db, @IsaObjectId(session = false) UUID id)
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
    public String getServiceState(BergamotDB db, @Var("site") Site site, @IsaObjectId(session = false) UUID id, @Param("status") String status, @Param("output") String output)
    {   
        Trap trap = db.getTrap(id);
        if (trap == null) return null;
        // the result
        PassiveResultMO resultMO = new PassiveResultMO();
        resultMO.setId(UUID.randomUUID());
        resultMO.setSiteId(site.getId());
        resultMO.setMatchOn(new MatchOnCheckId(id));
        resultMO.setStatus(status);
        resultMO.setOk("OK".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status));
        resultMO.setOutput(output);
        resultMO.setExecuted(System.currentTimeMillis());
        resultMO.setRuntime(0);
        resultMO.setParameter("bergamot.ui.instance", this.app().getInstanceName());
        // dispatch the result for processing
        action("dispatch-result", resultMO);
        return "Accepted";
    }
    
    @Get("/id/:id/suppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String suppressTrap(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Trap trap = db.getTrap(id);
        if (trap == null) throw new BalsaNotFound("No trap with id '" + id + "' exists.");
        action("suppress-check", trap);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressTrap(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Trap trap = db.getTrap(id);
        if (trap == null) throw new BalsaNotFound("No trap with id '" + id + "' exists.");
        action("unsuppress-check", trap);
        return "Ok";
    }
    
    @Get("/name/:host/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TrapCfg getTrapConfig(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {
        return Util.nullable(db.getTrapOnHostByName(site.getId(), hostName, name), Trap::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TrapCfg getTrapConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getTrap(id), Trap::getConfiguration);
    }
}

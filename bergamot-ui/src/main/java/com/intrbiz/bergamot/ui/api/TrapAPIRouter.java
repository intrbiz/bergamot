package com.intrbiz.bergamot.ui.api;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.processor.result.PassiveResult;
import com.intrbiz.bergamot.model.message.processor.result.match.MatchOnCheckId;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.bergamot.ui.BergamotUI;
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
public class TrapAPIRouter extends Router<BergamotUI>
{    
    @Get("/name/:host/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TrapMO getTrapByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {   
        Trap trap = notNull(db.getTrapOnHostByName(site.getId(), hostName, name));
        require(permission("read", trap));
        return trap.toMO(currentPrincipal());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TrapMO getTrap(BergamotDB db, @IsaObjectId() UUID id)
    {
        Trap trap = notNull(db.getTrap(id));
        require(permission("read", trap));
        return trap.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getTrapState(BergamotDB db, @IsaObjectId() UUID id)
    {
        Trap trap = notNull(db.getTrap(id));
        require(permission("read", trap));
        return trap.getState().toMO(currentPrincipal());
    }
    
    @Get("/name/:host/:name/state")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CheckStateMO getTrapStateByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {    
        Trap trap = notNull(db.getTrapOnHostByName(site.getId(), hostName, name));
        require(permission("read", trap));
        return trap.getState().toMO(currentPrincipal());
    }
    
    @Any("/id/:id/submit")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public String submitTrapStatus(BergamotDB db, @Var("site") Site site, @IsaObjectId() UUID id, @Param("status") String status, @Param("output") String output)
    {   
        Trap trap = notNull(db.getTrap(id));
        require(permission("submit", trap));
        // the result
        PassiveResult resultMO = new PassiveResult();
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
    public String suppressTrap(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Trap trap = notNull(db.getTrap(id));
        require(permission("suppress", trap));
        action("suppress-check", trap);
        return "Ok";
    }
    
    @Get("/id/:id/unsuppress")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String unsuppressTrap(BergamotDB db, @IsaObjectId() UUID id)
    { 
        Trap trap = notNull(db.getTrap(id));
        require(permission("unsuppress", trap));
        action("unsuppress-check", trap);
        return "Ok";
    }
    
    @Get("/name/:host/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public TrapCfg getTrapConfigByName(BergamotDB db, @Var("site") Site site, String hostName, String name)
    {
        Trap trap = notNull(db.getTrapOnHostByName(site.getId(), hostName, name));
        require(permission("read.config", trap));
        return trap.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public TrapCfg getTrapConfig(BergamotDB db, @IsaObjectId() UUID id)
    {
        Trap trap = notNull(db.getTrap(id));
        require(permission("read.config", trap));
        return trap.getConfiguration();
    }
}

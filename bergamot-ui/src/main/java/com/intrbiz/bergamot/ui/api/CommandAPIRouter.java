package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/command")
@RequireValidPrincipal()
public class CommandAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @RequirePermission("api.read.command")
    @WithDataAdapter(BergamotDB.class)
    public List<CommandMO> getCommand(BergamotDB db, @Var("site") Site site)
    {
        return db.listCommands(site.getId()).stream().map(Command::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.command")
    @WithDataAdapter(BergamotDB.class)
    public CommandMO getCommand(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getCommandByName(site.getId(), name), Command::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @RequirePermission("api.read.command")
    @WithDataAdapter(BergamotDB.class)
    public CommandMO getCommand(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCommand(id), Command::toMO);
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.command.config")
    @WithDataAdapter(BergamotDB.class)
    public CommandCfg getCommandConfig(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getCommandByName(site.getId(), name), Command::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermission("api.read.command.config")
    @WithDataAdapter(BergamotDB.class)
    public CommandCfg getCommandConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        return Util.nullable(db.getCommand(id), Command::getConfiguration);
    }
}

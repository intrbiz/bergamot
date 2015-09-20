package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/command")
@RequireValidPrincipal()
public class CommandAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(CommandMO.class)
    public List<CommandMO> getCommands(BergamotDB db, @Var("site") Site site)
    {
        return db.listCommands(site.getId()).stream().filter((c) -> permission("read", c)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CommandMO getCommandByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Command command = notNull(db.getCommandByName(site.getId(), name));
        require(permission("read", command));
        return command.toMO(currentPrincipal());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public CommandMO getCommand(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Command command = notNull(db.getCommand(id));
        require(permission("read", command));
        return command.toMO(currentPrincipal());
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public CommandCfg getCommandConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Command command = notNull(db.getCommandByName(site.getId(), name));
        require(permission("read.config", command));
        return command.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public CommandCfg getCommandConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Command command = notNull(db.getCommand(id));
        require(permission("read.config", command));
        return command.getConfiguration();
    }
}

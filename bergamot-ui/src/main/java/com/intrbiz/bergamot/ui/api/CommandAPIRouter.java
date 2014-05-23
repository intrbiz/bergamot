package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/command")
public class CommandAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<CommandMO> getCommand()
    {
        return this.app().getBergamot().getObjectStore().getCommands().stream().map(Command::toMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public CommandMO getCommand(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupCommand(name), Command::toMO);
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public CommandMO getCommand(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupCommand(id), Command::toMO);
    }
}

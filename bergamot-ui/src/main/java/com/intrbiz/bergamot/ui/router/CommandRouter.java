package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/command")
@Template("layout/main")
@RequireValidPrincipal()
public class CommandRouter extends Router<BergamotApp>
{    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void command(BergamotDB db, @IsaObjectId UUID id)
    {
        Command command = model("command", notNull(db.getCommand(id)));
        require(permission("read", command));
        encode("command/detail");
    }
}

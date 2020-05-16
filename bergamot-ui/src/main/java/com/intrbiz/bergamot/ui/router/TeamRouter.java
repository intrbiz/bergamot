package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/team")
@Template("layout/main")
@RequireValidPrincipal()
public class TeamRouter extends Router<BergamotUI>
{    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void team(BergamotDB db, @IsaObjectId UUID id)
    {
        Team team = model("team", notNull(db.getTeam(id)));
        require(permission("read", team));
        encode("team/detail");
    }
}

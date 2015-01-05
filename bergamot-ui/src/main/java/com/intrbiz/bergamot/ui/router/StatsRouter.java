package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/stats")
@Template("layout/main")
@RequireValidPrincipal()
public class StatsRouter extends Router<BergamotApp>
{    
    @Any("/transitions/check/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void trap(BergamotDB db, @AsUUID UUID id)
    {
        model("check", db.getCheck(id));
        model("transitions", db.listCheckTransitionsForCheck(id, 0, 100));
        encode("stats/transitions");
    }
}

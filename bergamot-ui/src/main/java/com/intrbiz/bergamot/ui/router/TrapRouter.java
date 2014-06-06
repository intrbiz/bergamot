package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/trap")
@Template("layout/main")
@RequireValidPrincipal()
public class TrapRouter extends Router<BergamotApp>
{    
    @Any("/name/:host/:trap")
    @WithDataAdapter(BergamotDB.class)
    public void trap(BergamotDB db, String hostName, String trapName, @SessionVar("site") Site site)
    {
        model("trap", db.getTrapOnHostByName(site.getId(), hostName, trapName));
        encode("trap/detail");
    }
    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void trap(BergamotDB db, @AsUUID UUID id)
    {
        model("trap", db.getTeam(id));
        encode("trap/detail");
    }
    
    @Any("/enable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void enableTrap(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Trap trap = db.getTrap(id);
        if (trap != null)
        {
            trap.setEnabled(true);
            db.setTrap(trap);
        }
        redirect("/trap/id/" + id);
    }
    
    @Any("/disable/:id")
    @WithDataAdapter(BergamotDB.class)
    public void disableTrap(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Trap trap = db.getTrap(id);
        if (trap != null)
        {
            trap.setEnabled(false);
            db.setTrap(trap);
        }
        redirect("/trap/id/" + id);
    }
    
    @Any("/suppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void suppressTrap(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Trap trap = db.getTrap(id);
        if (trap != null)
        {
            trap.setSuppressed(true);
            db.setTrap(trap);
        }
        redirect("/trap/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    @WithDataAdapter(BergamotDB.class)
    public void unsuppressTrap(BergamotDB db, @AsUUID UUID id) throws IOException
    {
        Trap trap = db.getTrap(id);
        if (trap != null)
        {
            trap.setSuppressed(false);
            db.setTrap(trap);
        }
        redirect("/trap/id/" + id);
    }
}

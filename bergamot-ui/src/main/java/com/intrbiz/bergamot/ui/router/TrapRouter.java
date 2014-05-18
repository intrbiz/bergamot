package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/trap")
@Template("layout/main")
public class TrapRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/name/:host/:trap")
    public void trap(String hostName, String trapName)
    {
        Bergamot bergamot = this.getBergamot();
        Host host = bergamot.getObjectStore().lookupHost(hostName);
        model("trap", host.getTrap(trapName));
        encode("trap/detail");
    }
    
    @Any("/id/:id")
    public void trap(@AsUUID UUID id)
    {
        Bergamot bergamot = this.getBergamot();
        model("trap", bergamot.getObjectStore().lookupTrap(id));
        encode("trap/detail");
    }
    
    @Any("/enable/:id")
    public void enableTrap(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the trap and enable it
        Trap trap = bergamot.getObjectStore().lookupTrap(id);
        if (trap != null)
        {
            trap.setEnabled(true);
        }
        redirect("/trap/id/" + id);
    }
    
    @Any("/disable/:id")
    public void disableTrap(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the trap and disable it
        Trap trap = bergamot.getObjectStore().lookupTrap(id);
        if (trap != null)
        {
            trap.setEnabled(false);
        }
        redirect("/trap/id/" + id);
    }
    
    @Any("/suppress/:id")
    public void suppressTrap(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the trap and supress it
        Trap trap = bergamot.getObjectStore().lookupTrap(id);
        if (trap != null)
        {
            // suppress the trap
            trap.setSuppressed(true);
            bergamot.getObjectStore().removeAlert(trap);
        }
        redirect("/trap/id/" + id);
    }
    
    @Any("/unsuppress/:id")
    public void unsuppressTrap(@AsUUID UUID id) throws IOException
    {
        Bergamot bergamot = this.getBergamot();
        // get the trap and unsupress it
        Trap trap = bergamot.getObjectStore().lookupTrap(id);
        if (trap != null)
        {
            // unsuppress the trap
            trap.setSuppressed(false);
        }
        redirect("/trap/id/" + id);
    }
}

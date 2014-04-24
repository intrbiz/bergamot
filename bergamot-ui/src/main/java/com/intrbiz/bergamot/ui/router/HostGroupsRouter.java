package com.intrbiz.bergamot.ui.router;

import java.io.IOException;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.HostGroup;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

@Prefix("/")
@Template("layout/main")
public class HostGroupsRouter extends Router
{
    private Bergamot getBergamot()
    {
        return ((BergamotApp) this.app()).getBergamot();
    }
    
    @Any("/hostgroup")
    public void rediectHostGroups() throws IOException
    {
        redirect("/hostgroup/");
    }
    
    @Any("/hostgroup/")
    public void showHostGroups()
    {
        Bergamot bergamot = this.getBergamot();
        model("hostgroups", orderHostGroupsByStatus(bergamot.getObjectStore().getHostgroups()));
        encode("hostgroup/index");
    }
    
    @Any("/hostgroup/name/:name")
    public void showHostGroupByName(String name)
    {
        Bergamot bergamot = this.getBergamot();
        HostGroup hostGroup = model("hostgroup", bergamot.getObjectStore().lookupHostgroup(name));
        model("hosts", orderHostsByStatus(hostGroup.getHosts()));
        encode("hostgroup/hosts");
    }
}

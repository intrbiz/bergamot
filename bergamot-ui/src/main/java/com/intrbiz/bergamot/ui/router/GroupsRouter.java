package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.io.IOException;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
public class GroupsRouter extends Router<BergamotApp>
{    
    @Any("/group")
    public void rediectGroups() throws IOException
    {
        redirect("/group/");
    }
    
    @Any("/group/")
    public void showGroups()
    {
        Bergamot bergamot = this.app().getBergamot();
        model("groups", orderGroupsByStatus(bergamot.getObjectStore().getGroups().stream().filter((e) -> {return e.getParents().isEmpty();}).collect(Collectors.toList())));
        encode("group/index");
    }
    
    @Any("/group/name/:name")
    public void showHostGroupByName(String name)
    {
        Bergamot bergamot = this.app().getBergamot();
        Group group = model("group", bergamot.getObjectStore().lookupGroup(name));
        model("checks", orderCheckByStatus(group.getChecks()));
        model("groups", orderGroupsByStatus(group.getChildren()));
        encode("group/group");
    }
}

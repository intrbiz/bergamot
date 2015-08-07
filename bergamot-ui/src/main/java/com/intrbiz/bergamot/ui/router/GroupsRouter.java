package com.intrbiz.bergamot.ui.router;

import static com.intrbiz.bergamot.ui.util.Sorter.*;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/")
@Template("layout/main")
@RequireValidPrincipal()
public class GroupsRouter extends Router<BergamotApp>
{    
    @Any("/group")
    public void rediectGroups() throws IOException
    {
        redirect("/group/");
    }
    
    @Any("/group/")
    @WithDataAdapter(BergamotDB.class)
    public void showGroups(BergamotDB db, @SessionVar("site") Site site)
    {
        model("groups", orderGroupsByStatus(permission("read", db.getRootGroups(site.getId()))));
        encode("group/index");
    }
    
    @Any("/group/name/:name")
    @WithDataAdapter(BergamotDB.class)
    public void showHostGroupByName(BergamotDB db, String name, @SessionVar("site") Site site)
    {
        Group group = model("group", notNull(db.getGroupByName(site.getId(), name)));
        require(permission("read", group));
        model("checks", orderCheckByStatus(permission("read", group.getChecks())));
        model("groups", orderGroupsByStatus(permission("read", group.getChildren())));
        encode("group/group");
    }
    
    @Any("/group/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void showHostGroupByName(BergamotDB db, @IsaObjectId UUID id, @CurrentPrincipal Contact user)
    {
        Group group = model("group", notNull(db.getGroup(id)));
        require(permission("read", group));
        model("checks", orderCheckByStatus(permission("read", group.getChecks())));
        model("groups", orderGroupsByStatus(permission("read", group.getChildren())));
        encode("group/group");
    }
    
    @Any("/group/execute-all-checks/:id")
    @WithDataAdapter(BergamotDB.class)
    public void executeChecksInGroup(BergamotDB db, @IsaObjectId UUID id) throws IOException
    {
        for (Check<?,?> check : db.getChecksInGroup(id))
        {
            if (check instanceof ActiveCheck)
            {
                if (permission("execute", check)) action("execute-check", check);
            }
        }
        redirect("/group/id/" + id);
    }
}

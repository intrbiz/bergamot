package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/sla")
@Template("layout/main")
@RequireValidPrincipal()
public class SLARouter extends Router<BergamotApp>
{    
    @Any("/group/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void slaGroup(
            BergamotDB db, 
            @IsaObjectId UUID id
    )
    {
        Group group = model("group", notNull(db.getGroup(id)));
        require(permission("read", group));
        model("slas", db.buildSLAReportForGroup(group.getId(), false));
        encode("sla/group");
    }
    
    @Any("/check/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void slaCheck(
            BergamotDB db, 
            @IsaObjectId UUID id
    )
    {
        Check<?,?> check = model("check", notNull(db.getCheck(id)));
        require(permission("read", check));
        model("slas", db.buildSLAReportForCheck(check.getId(), false));
        encode("sla/check");
    }
}

package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/config")
@Template("layout/main")
@RequireValidPrincipal()
public class ConfigRouter extends Router<BergamotUI>
{   
    @Any("/check/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @IsaObjectId UUID id)
    {
        Check<?,?> check = model("check", notNull(db.getCheck(id)));
        require(permission("read", check));
        require(permission("read.config", check));
        encode("config/index");
    }
}

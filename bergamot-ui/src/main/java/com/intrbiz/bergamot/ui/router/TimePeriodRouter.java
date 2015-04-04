package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/timeperiod")
@Template("layout/main")
@RequireValidPrincipal()
public class TimePeriodRouter extends Router<BergamotApp>
{    
    @Any("/id/:id")
    @RequirePermission("ui.config.view")
    @WithDataAdapter(BergamotDB.class)
    public void timePeriod(BergamotDB db, @IsaObjectId UUID id)
    {
        model("timeperiod", db.getTimePeriod(id));
        encode("timeperiod/detail");
    }
}

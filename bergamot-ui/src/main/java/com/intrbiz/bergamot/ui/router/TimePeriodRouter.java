package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/timeperiod")
@Template("layout/main")
@RequireValidPrincipal()
public class TimePeriodRouter extends Router<BergamotApp>
{    
    @Any("/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void timePeriod(BergamotDB db, @IsaObjectId UUID id)
    {
        TimePeriod timePeriod = model("timeperiod", notNull(db.getTimePeriod(id)));
        require(permission("read", timePeriod));
        encode("timeperiod/detail");
    }
}

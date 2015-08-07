package com.intrbiz.bergamot.ui.router;

import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.Param;
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
    public void trap(
            BergamotDB db, 
            @IsaObjectId UUID id, 
            @Param("offset") @IsaLong(min = 0, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 0L)   long offset,
            @Param("limit")  @IsaLong(min = 1, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 100L) long limit
    )
    {
        Check<?,?> check = model("check", notNull(db.getCheck(id)));
        require(permission("read", check));
        model("transitions", db.listCheckTransitionsForCheck(id, offset, limit));
        var("offset", offset);
        var("limit", limit);
        encode("stats/transitions");
    }
}

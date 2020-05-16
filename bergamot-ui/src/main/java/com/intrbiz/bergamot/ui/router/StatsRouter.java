package com.intrbiz.bergamot.ui.router;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.ui.BergamotUI;
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
public class StatsRouter extends Router<BergamotUI>
{   
    @Any("/check/id/:id")
    @WithDataAdapter(BergamotDB.class)
    public void index(
            BergamotDB db, 
            @IsaObjectId UUID id, 
            @Param("limit")  @IsaLong(min = 1, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 100L) Long limit
    )
    {
        Check<?,?> check = model("check", notNull(db.getCheck(id)));
        require(permission("read", check));
        require(permission("ui.view.stats", check));
        var("transitions", db.listOlderCheckTransitionsForCheck(id, new Timestamp(System.currentTimeMillis()), limit));
        var("limit", limit);
        encode("stats/index");
    }
    
    @Any("/check/id/:id/older")
    @WithDataAdapter(BergamotDB.class)
    public void older(
            BergamotDB db, 
            @IsaObjectId UUID id, 
            @Param("from") @IsaLong(min = 0, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 0L)   Long from,
            @Param("limit")  @IsaLong(min = 1, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 100L) Long limit
    )
    {
        Check<?,?> check = model("check", notNull(db.getCheck(id)));
        require(permission("read", check));
        require(permission("ui.view.stats", check));
        if (from <= 0) from = System.currentTimeMillis();
        var("transitions", db.listOlderCheckTransitionsForCheck(id, new Timestamp(from), limit));
        var("from", from);
        var("limit", limit);
        encode("stats/index");
    }
    
    @Any("/check/id/:id/newer")
    @WithDataAdapter(BergamotDB.class)
    public void newer(
            BergamotDB db, 
            @IsaObjectId UUID id, 
            @Param("from") @IsaLong(min = 0, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 0L)   Long from,
            @Param("limit")  @IsaLong(min = 1, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 100L) Long limit
            
    )
    {
        Check<?,?> check = model("check", notNull(db.getCheck(id)));
        require(permission("read", check));
        require(permission("ui.view.stats", check));
        if (from <= 0) from = System.currentTimeMillis();
        var("transitions", db.listNewerCheckTransitionsForCheck(id, new Timestamp(from), limit));
        var("from", from);
        var("limit", limit);
        encode("stats/index");
    }
}

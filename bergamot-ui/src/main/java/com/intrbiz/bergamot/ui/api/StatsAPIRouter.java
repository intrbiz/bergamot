package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.message.state.CheckTransitionMO;
import com.intrbiz.bergamot.model.state.CheckTransition;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;

@Prefix("/api/stats")
@RequireValidPrincipal()
public class StatsAPIRouter extends Router<BergamotApp>
{    
    @Any("/transitions/check/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<CheckTransitionMO> trap(
            BergamotDB db, 
            @IsaObjectId(session = false) UUID id, 
            @Param("offset") @IsaLong(min = 0, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 0L)   long offset,
            @Param("limit")  @IsaLong(min = 1L, max = 1001L, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 100L) long limit
    )
    {
        return db.listCheckTransitionsForCheck(id, offset, limit).stream().map(CheckTransition::toMO).collect(Collectors.toList());
    }
}

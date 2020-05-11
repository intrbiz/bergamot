package com.intrbiz.bergamot.ui.api;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.message.state.CheckTransitionMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.IsaLong;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;

@Title("Stats API Methods")
@Desc({
    "Bergamot Monitoring tracks basic statistics about the execution of all checks, providing information on the performance of checks."
})
@Prefix("/api/stats")
@RequireValidPrincipal()
public class StatsAPIRouter extends Router<BergamotApp>
{
    @Title("Check Transitions")
    @Desc({
        "Get details of recent transitions for the check identified by the given UUID.  This will provide detailed data on every execution of a check."
    })
    @Any("/transitions/check/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(CheckTransitionMO.class)
    public List<CheckTransitionMO> getCheckTransitions(
            BergamotDB db, 
            @IsaObjectId() UUID id, 
            @Param("from") @IsaLong(min = 0, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 0L) Long from,
            @Param("limit")  @IsaLong(min = 1L, max = 1001L, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = 100L) Long limit
    )
    {
        require(permission("read", id));
        if (from <= 0) from = System.currentTimeMillis();
        return db.listOlderCheckTransitionsForCheck(id, new Timestamp(from), limit).stream().map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
}

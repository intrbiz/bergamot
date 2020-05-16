package com.intrbiz.bergamot.ui.api;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;


@Title("Agent API Methods")
@Desc({
    "The Bergamot Agent runs on servers and allows checks to be run locally on a host."
})
@Prefix("/api/agent")
@RequireValidPrincipal()
@RequirePermission("api.sign.agent")
@RequirePermission("sign.agent")
public class AgentAPIRouter extends Router<BergamotUI>
{
    
    /**
     * Revoke an agent key
     */
    @Title("Revoke agent key")
    @Desc({
        "Revoke all agents which are using the given agent key"
    })
    @Any("/revoke-agent-key")   
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public String revokeAgent(BergamotDB db, @Var("site") Site site, @Param("id") @IsaObjectId UUID agentKeyId) throws IOException
    {
        db.setAgentKey(notNull(db.getAgentKey(agentKeyId), "No such agent key: " + agentKeyId).revoke());
        return "revoked";
    }
}

package com.intrbiz.bergamot.ui.router.agent;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.AgentKey;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/agent")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.sign.agent")
@RequirePermission("sign.agent")
public class AgentRouter extends Router<BergamotApp>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void listAgents(BergamotDB db, @GetBergamotSite() Site site)
    {
        model("agentKeys", db.listAgentKeys(site.getId()));
        encode("agent/index");
    }
    
    @Any("/create")
    @WithDataAdapter(BergamotDB.class)
    public void createAgentKey(BergamotDB db, @GetBergamotSite() Site site, @Param("purpose") @CheckStringLength(min = 1, max = 100, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = "General Agent Key") String purpose) throws IOException
    {
        db.setAgentKey(AgentKey.create(site.getId(), purpose));
        // display
        redirect(path("/agent/"));
    }
    
    @Any("/revoke/:id")
    @WithDataAdapter(BergamotDB.class)
    public void revokeAgent(BergamotDB db, @GetBergamotSite() Site site, @Param("id") @IsaObjectId() UUID agentKeyId) throws Exception
    {
        db.setAgentKey(notNull(db.getAgentKey(agentKeyId)).revoke());
        // encode the index
        redirect(path("/agent/"));
    }
}

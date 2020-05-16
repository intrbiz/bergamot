package com.intrbiz.bergamot.ui.router.proxy;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.ProxyKey;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/proxy")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.sign.proxy")
@RequirePermission("sign.proxy")
public class ProxyRouter extends Router<BergamotUI>
{    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void listAgents(BergamotDB db, @GetBergamotSite() Site site)
    {
        var("proxyKeys", db.listProxyKeys(site.getId()));
        encode("proxy/index");
    }
    
    @Any("/create")
    @WithDataAdapter(BergamotDB.class)
    public void createAgentKey(BergamotDB db, @GetBergamotSite() Site site, @Param("purpose") @CheckStringLength(min = 1, max = 100, mandatory = true, coalesce = CoalesceMode.ALWAYS, defaultValue = "General Proxy Key") String purpose) throws IOException
    {
        db.setProxyKey(ProxyKey.create(site.getId(), purpose));
        // display
        redirect(path("/proxy/"));
    }
    
    @Any("/revoke/:id")
    @WithDataAdapter(BergamotDB.class)
    public void revokeAgent(BergamotDB db, @GetBergamotSite() Site site, @Param("id") @IsaObjectId() UUID proxyKeyId) throws Exception
    {
        db.setProxyKey(notNull(db.getProxyKey(proxyKeyId)).revoke());
        // encode the index
        redirect(path("/proxy/"));
    }
}

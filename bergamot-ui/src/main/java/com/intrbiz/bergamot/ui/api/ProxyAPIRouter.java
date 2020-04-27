package com.intrbiz.bergamot.ui.api;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.doc.Desc;
import com.intrbiz.metadata.doc.Title;


@Title("Proxy API Methods")
@Desc({
    "The Bergamot Proxy supports remote worker and notifier nodes."
})
@Prefix("/api/proxy")
@RequireValidPrincipal()
@RequirePermission("api.sign.proxy")
@RequirePermission("sign.proxy")
public class ProxyAPIRouter extends Router<BergamotApp>
{
    
    /**
     * Revoke an proxy key
     */
    @Title("Revoke proxy key")
    @Desc({
        "Revoke all proxies which are using the given proxy key"
    })
    @Any("/revoke-proxy-key")   
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public String revokeAgent(BergamotDB db, @Var("site") Site site, @Param("id") @IsaObjectId UUID proxyKeyId) throws IOException
    {
        db.setProxyKey(notNull(db.getProxyKey(proxyKeyId), "No such proxy key: " + proxyKeyId).revoke());
        return "revoked";
    }
}

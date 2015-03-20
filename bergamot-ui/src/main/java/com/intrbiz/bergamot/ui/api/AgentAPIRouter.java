package com.intrbiz.bergamot.ui.api;

import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;



@Prefix("/api/agent")
@RequireValidPrincipal()
public class AgentAPIRouter extends Router<BergamotApp>
{    
    /**
     * Sign an agent key
     */
    @Any("/sign-agent")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<String> signAgent(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("certificate-request") @CheckStringLength(min = 1, max = 4096, mandatory = true) String certReq
    )
    {
        // assign the agent UUID
        UUID agentId = Site.randomId(site.getId());
        // get the Root CA Certificate
        Certificate rootCrt  = action("get-root-ca");
        // get the Site CA Certificate
        Certificate siteCrt  = action("get-site-ca", site.getId());
        // ok, actually sign the agent certificate
        Certificate agentCrt = action("sign-agent", site.getId(), agentId, certReq);
        // return the certificate chain
        return Arrays.asList(new String[] {
                PEMUtil.saveCertificate(agentCrt),
                PEMUtil.saveCertificate(siteCrt),
                PEMUtil.saveCertificate(rootCrt),
        });
    }
}

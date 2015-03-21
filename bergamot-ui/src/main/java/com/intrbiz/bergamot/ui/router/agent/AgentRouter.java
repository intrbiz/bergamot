package com.intrbiz.bergamot.ui.router.agent;

import java.security.cert.Certificate;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.agent.config.BergamotAgentCfg;
import com.intrbiz.bergamot.crypto.util.CertificatePair;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Post;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
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
    public void listAgents(BergamotDB db, @SessionVar("site") Site site)
    {
        model("agents", db.listAgentRegistrations(site.getId()));
        encode("agent/index");
    }
    
    @Get("/generate-config")
    @RequirePermission("ui.generate.agent")
    public void showGenerateAgentConfig()
    {
        encode("agent/generate-config");
    }
    
    @Get("/sign")
    public void showSignAgentConfig()
    {
        encode("agent/sign-agent");
    }
    
    @Post("/generate-config")
    @RequirePermission("ui.generate.agent")
    @WithDataAdapter(BergamotDB.class)
    public void generateAgentConfig(BergamotDB db, @SessionVar("site") Site site, @Param("common-name") @CheckStringLength(min = 1, max = 255, mandatory = true) String commonName)
    {
        UUID agentId = var("agentId", Site.randomId(site.getId()));
        var("commonName", commonName);
        // generate
        Certificate     rootCert = action("get-root-ca");
        Certificate     siteCert = action("get-site-ca", site.getId());
        CertificatePair pair     = action("generate-agent", site.getId(), agentId, commonName);
        // build the config
        BergamotAgentCfg cfg = new BergamotAgentCfg();
        cfg.setCaCertificate(padCert(PEMUtil.saveCertificate(rootCert)));
        cfg.setSiteCaCertificate(padCert(PEMUtil.saveCertificate(siteCert)));
        cfg.setCertificate(padCert(pair.getCertificateAsPEM()));
        cfg.setKey(padCert(pair.getKeyAsPEM()));
        cfg.setName(commonName);
        cfg.addParameter(new CfgParameter("agent-id", null, null, agentId.toString()));
        System.out.println(cfg.toString());
        var("agentConfig", cfg.toString() + "\n<!-- Agent: UUID=" + agentId + " CN=" + commonName + " -->");
        encode("agent/generated-config");
    }
    
    @Post("/sign")
    @WithDataAdapter(BergamotDB.class)
    public void signAgent(BergamotDB db, @SessionVar("site") Site site, @Param("certificate-request") @CheckStringLength(min = 1, max = 16384, mandatory = true) String certReq)
    {
        UUID agentId = var("agentId", Site.randomId(site.getId()));
        // sign
        Certificate rootCrt  = action("get-root-ca");
        Certificate siteCrt  = action("get-site-ca", site.getId());
        Certificate agentCrt = action("sign-agent", site.getId(), agentId, certReq);
        var("agentCrt",  PEMUtil.saveCertificate(agentCrt));
        var("siteCaCrt", PEMUtil.saveCertificate(siteCrt));
        var("caCrt",     PEMUtil.saveCertificate(rootCrt));
        encode("agent/signed-agent");
    }
    
    private static String padCert(String cert)
    {
        StringBuilder sb = new StringBuilder("\r\n");
        for (String s : cert.split("\n"))
        {
            sb.append("        ").append(s).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}

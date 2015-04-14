package com.intrbiz.bergamot.ui.router.agent;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.agent.config.BergamotAgentCfg;
import com.intrbiz.bergamot.agent.config.CfgParameter;
import com.intrbiz.bergamot.crypto.util.CertificatePair;
import com.intrbiz.bergamot.crypto.util.CertificateRequest;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.SerialNum;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.AgentRegistration;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
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
        // is an agent already registered
        AgentRegistration agentReg = db.getAgentRegistrationByName(site.getId(), commonName);
        if (agentReg != null) throw new RuntimeException("Cannot generate configuration for an agent which already exists!");
        // assign id
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
        // store the agent registration
        db.setAgentRegistration(new AgentRegistration(site.getId(), agentId, commonName, SerialNum.fromBigInt(pair.getCertificate().getSerialNumber()).toString()));
        // display
        var("agentConfig", cfg.toString() + "\n<!-- Agent: UUID=" + agentId + " CN=" + commonName + " -->");
        encode("agent/generated-config");
    }
    
    @Post("/sign")
    @WithDataAdapter(BergamotDB.class)
    public void signAgent(BergamotDB db, @SessionVar("site") Site site, @Param("certificate-request") @CheckStringLength(min = 1, max = 16384, mandatory = true) String certReq) throws IOException
    {
        // parse the certificate request
        CertificateRequest req = PEMUtil.loadCertificateRequest(certReq);
        // is an agent already registered
        AgentRegistration agentReg = db.getAgentRegistrationByName(site.getId(), req.getCommonName());
        if (agentReg != null) throw new RuntimeException("Cannot generate configuration for an agent which already exists!");
        // generate agent it
        UUID agentId = var("agentId", Site.randomId(site.getId()));
        // sign
        Certificate rootCrt  = action("get-root-ca");
        Certificate siteCrt  = action("get-site-ca", site.getId());
        Certificate agentCrt = action("sign-agent", site.getId(), agentId, req);
        // store the registration
        db.setAgentRegistration(new AgentRegistration(site.getId(), agentId, req.getCommonName(), SerialNum.fromBigInt(((X509Certificate) agentCrt).getSerialNumber()).toString()));
        // display
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

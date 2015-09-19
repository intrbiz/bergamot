package com.intrbiz.bergamot.ui.api;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.crypto.util.CertificateRequest;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.SerialNum;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.model.AgentRegistration;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;



@Prefix("/api/agent")
@RequireValidPrincipal()
@RequirePermission("api.sign.agent")
@RequirePermission("sign.agent")
public class AgentAPIRouter extends Router<BergamotApp>
{    
    /**
     * Sign an agent certificate request
     */
    @Any("/sign-agent")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public List<String> signAgent(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("certificate-request") @CheckStringLength(min = 1, max = 16384, mandatory = true) String certReq
    ) throws IOException
    {
        // parse the certificate request
        CertificateRequest req = PEMUtil.loadCertificateRequest(certReq);
        // is an agent already registered
        AgentRegistration agentReg = db.getAgentRegistrationByName(site.getId(), req.getCommonName());
        if (agentReg != null) throw new RuntimeException("Cannot generate configuration for an agent which already exists!");
        // assign the agent UUID
        UUID agentId = Site.randomId(site.getId());
        // get the Root CA Certificate
        Certificate rootCrt  = action("get-root-ca");
        // get the Site CA Certificate
        Certificate siteCrt  = action("get-site-ca", site.getId());
        // ok, actually sign the agent certificate
        Certificate agentCrt = action("sign-agent", site.getId(), agentId, req);
        // store the registration
        db.setAgentRegistration(new AgentRegistration(site.getId(), agentId, req.getCommonName(), SerialNum.fromBigInt(((X509Certificate) agentCrt).getSerialNumber()).toString()));
        // return the certificate chain
        return Arrays.asList(new String[] {
                PEMUtil.saveCertificate(agentCrt),
                PEMUtil.saveCertificate(siteCrt),
                PEMUtil.saveCertificate(rootCrt),
        });
    }
    
    /**
     * Sign an agent key
     */
    @Any("/sign-agent-key")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public List<String> signAgentKey(
            BergamotDB db, 
            @Var("site") Site site,
            @Param("common-name") @CheckStringLength(min = 1, max = 255, mandatory = true)   String commonName,
            @Param("public-key")  @CheckStringLength(min = 1, max = 16384, mandatory = true) String publicKey
    ) throws IOException
    {
        // is an agent already registered
        AgentRegistration agentReg = db.getAgentRegistrationByName(site.getId(), commonName);
        if (agentReg != null) throw new RuntimeException("Cannot generate configuration for an agent which already exists!");
        // decode the key
        PublicKey key = PEMUtil.loadPublicKey(publicKey);
        // assign the agent UUID
        UUID agentId = Site.randomId(site.getId());
        // get the Root CA Certificate
        Certificate rootCrt  = action("get-root-ca");
        // get the Site CA Certificate
        Certificate siteCrt  = action("get-site-ca", site.getId());
        // ok, actually sign the agent certificate
        Certificate agentCrt = action("sign-agent-key", site.getId(), agentId, commonName, key);
        // store the registration
        db.setAgentRegistration(new AgentRegistration(site.getId(), agentId, commonName, SerialNum.fromBigInt(((X509Certificate) agentCrt).getSerialNumber()).toString()));
        // return the certificate chain
        return Arrays.asList(new String[] {
                PEMUtil.saveCertificate(agentCrt),
                PEMUtil.saveCertificate(siteCrt),
                PEMUtil.saveCertificate(rootCrt),
        });
    }
}

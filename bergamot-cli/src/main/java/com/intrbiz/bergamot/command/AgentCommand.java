package com.intrbiz.bergamot.command;

import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.agent.config.BergamotAgentCfg;
import com.intrbiz.bergamot.agent.config.CfgParameter;
import com.intrbiz.bergamot.config.CLICfg;
import com.intrbiz.bergamot.config.CLISiteCfg;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.RSAUtil;
import com.intrbiz.bergamot.crypto.util.SerialNum;

public class AgentCommand extends BergamotCLICommand
{
    public AgentCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "agent";
    }

    @Override
    public String usage()
    {
        return "(generate) ...";
    }

    @Override
    public String help()
    {
        return "Manager Bergamot Agent certificates\n" +
                "\n" +
                "Commands:\n" +
                "  generate <site-name> <common-name> - generate and sign a key pair for Bergamot Agent, returning a configuration file\n" +
                "\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() < 1) throw new BergamotCLIException("No command given");
        String command = args.remove(0);
        // process the sub command
        if ("generate".equalsIgnoreCase(command))
        {
            if (args.size() != 2) throw new BergamotCLIException("No site-name or common-name given");
            String siteName = args.remove(0);
            String commonName = args.remove(0);
            // get the site config
            CLISiteCfg site = CLICfg.loadConfiguration().getSite(siteName);
            if (site == null) throw new BergamotCLIException("No site configured with the name '" + siteName + "'");
            // connect to the API
            BergamotClient client = new BergamotClient(site.getUrl(), site.getAuthToken());
            // call the hello world test
            try
            {
                client.callHelloYou().execute();
            }
            catch (Exception e)
            {
                throw new BergamotCLIException("API connectivity test failed, bailing out.", e);
            }
            // generate a key pair
            KeyPair pair = RSAUtil.generateRSAKeyPair(2048);
            // sign the certificate
            List<Certificate> chain = client.callSignAgentKey().commonName(commonName).publicKey(pair.getPublic()).execute();
            Certificate agentCrt = chain.get(0);
            Certificate siteCrt  = chain.get(1);
            Certificate caCrt    = chain.get(2);
            // get the agent UUID
            SerialNum serial = SerialNum.fromBigInt(((X509Certificate) agentCrt).getSerialNumber());
            // generate the agent config
            BergamotAgentCfg cfg = new BergamotAgentCfg();
            cfg.setCaCertificate(PEMUtil.saveCertificate(caCrt));
            cfg.setSiteCaCertificate(PEMUtil.saveCertificate(siteCrt));
            cfg.setCertificate(PEMUtil.saveCertificate(agentCrt));
            cfg.setKey(PEMUtil.saveKey(pair.getPrivate()));
            cfg.setName(commonName);
            cfg.addParameter(new CfgParameter("agent-id", null, null, serial.getId().toString()));
            System.out.println(cfg.toString());
            System.out.println("<!-- Agent: UUID=" + serial.getId() + " CN=" + commonName + " -->");
            return 0;
        }
        else
        {
            throw new BergamotCLIException("Unknown sub command: " + command);
        }
    }
}

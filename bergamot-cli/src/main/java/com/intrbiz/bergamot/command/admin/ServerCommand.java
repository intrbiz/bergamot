package com.intrbiz.bergamot.command.admin;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.agent.server.config.BergamotAgentServerCfg;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.RSAUtil;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignServer;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedServer;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.bergamot.queue.util.QueueUtil;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;

public class ServerCommand extends BergamotCLICommand
{
    public ServerCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "server";
    }
    
    @Override
    public boolean admin()
    {
        return true;
    }

    @Override
    public String usage()
    {
        return "generate ...";
    }

    @Override
    public String help()
    {
        return "Manager Bergamot Agent Server certificates\n" +
                "\n" +
                "Commands:\n" +
                "  generate <common-name> - generate and sign a key pair for a Bergamot Agent Server, returning a configuration file\n" +
                "\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() < 1) throw new BergamotCLIException("No command given");
        String command = args.remove(0);
        if ("generate".equalsIgnoreCase(command))
        {
            if (args.size() != 1) throw new BergamotCLIException("No server common name given");
            String commonName = args.remove(0);
            // read the UI config and connect to the database
            UICfg config = UICfg.loadConfiguration();
            // setup the queue manager
            QueueUtil.setupQueueBroker(config.getBroker(), "bergamot-cli");
            // sign the server
            try (BergamotAgentManagerQueue queue = BergamotAgentManagerQueue.open())
            {
                try (RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client = queue.createBergamotAgentManagerRPCClient())
                {            
                    // get the root CA
                    Certificate rootCrt = this.getRootCA(client);
                    // generate a key pair
                    KeyPair pair = RSAUtil.generateRSAKeyPair(2048);
                    // sign the key
                    Certificate serverCrt = this.signServer(client, commonName, pair.getPublic());
                    // build the config
                    BergamotAgentServerCfg cfg = new BergamotAgentServerCfg();
                    cfg.setCaCertificate(PEMUtil.saveCertificate(rootCrt));
                    cfg.setCertificate(PEMUtil.saveCertificate(serverCrt));
                    cfg.setKey(PEMUtil.saveKey(pair.getPrivate()));
                    cfg.setPort(15443);
                    cfg.setName(commonName);
                    System.out.println(cfg.toString());
                    System.out.println("<!-- Server CN=" + commonName + " -->");
                    return 0;
                }
            }
        }
        else
        {
            throw new BergamotCLIException("Unknown sub command: " + command);
        }
    }
    
    private Certificate signServer(RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client, String commonName, PublicKey key) throws BergamotCLIException
    {
        try
        {
            AgentManagerResponse response = client.publish(new SignServer(commonName, PEMUtil.savePublicKey(key))).get(5, TimeUnit.SECONDS);
            if (response instanceof SignedServer)
            {
                return PEMUtil.loadCertificate(((SignedServer) response).getCertificatePEM());
            }
            else
            {
                throw new BergamotCLIException("Failed to communicate with Bergamot Agent Manager");
            }
        }
        catch (InterruptedException | ExecutionException | TimeoutException | IOException e)
        {
            throw new BergamotCLIException("Failed to communicate with Bergamot Agent Manager", e);
        }
    }
    
    private Certificate getRootCA(RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client) throws BergamotCLIException
    {
        try
        {
            AgentManagerResponse response = client.publish(new GetRootCA()).get(5, TimeUnit.SECONDS);
            if (response instanceof GotRootCA)
            {
                return PEMUtil.loadCertificate(((GotRootCA) response).getCertificatePEM());
            }
            else
            {
                throw new BergamotCLIException("Failed to communicate with Bergamot Agent Manager");
            }
        }
        catch (InterruptedException | ExecutionException | TimeoutException | IOException e)
        {
            throw new BergamotCLIException("Failed to communicate with Bergamot Agent Manager", e);
        }
    }
}

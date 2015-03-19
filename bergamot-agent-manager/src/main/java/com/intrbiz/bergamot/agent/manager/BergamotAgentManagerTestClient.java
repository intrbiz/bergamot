package com.intrbiz.bergamot.agent.manager;

import java.security.KeyPair;
import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.RSAUtil;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.CreateSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignAgent;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignServer;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedAgent;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedServer;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;
import com.intrbiz.queue.rabbit.RabbitPool;

public class BergamotAgentManagerTestClient
{
    public static void main(String[] args) throws Exception
    {
        // setup logging
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        // setup the queue broker
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool("amqp://127.0.0.1", "bergamot", "bergamot"));
        // queue
        BergamotAgentManagerQueue queue = BergamotAgentManagerQueue.open();
        RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client = queue.createBergamotAgentManagerRPCClient();
        // get root
        System.out.println(client.publish(new GetRootCA()).get());
        // create a site
        UUID siteId = UUID.fromString("90947610-44bc-4000-8000-000000000000");
        System.out.println(client.publish(new CreateSiteCA(siteId, "bergamot.local")).get());
        System.out.println(client.publish(new GetSiteCA(siteId)).get());
        //
        System.exit(1);
        // agent
        KeyPair pair = RSAUtil.generateRSAKeyPair(2048);
        SignedAgent signed = (SignedAgent) client.publish(new SignAgent(siteId, UUID.randomUUID(), "test.client", PEMUtil.savePublicKey(pair.getPublic()))).get();
        System.out.println(signed);
        System.out.println(PEMUtil.saveKey(pair.getPrivate()));
        // server
        KeyPair pair1 = RSAUtil.generateRSAKeyPair(2048);
        SignedServer signed1 = (SignedServer) client.publish(new SignServer(siteId, "test.server", PEMUtil.savePublicKey(pair1.getPublic()))).get();
        System.out.println(signed1);
        System.out.println(PEMUtil.saveKey(pair1.getPrivate()));
    }
}

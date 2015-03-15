package com.intrbiz.bergamot.agent.manager;

import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.CreateSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetSiteCA;
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
        UUID siteId = UUID.randomUUID();
        System.out.println(client.publish(new CreateSiteCA(siteId, "Test Site")).get());
        System.out.println(client.publish(new GetSiteCA(siteId)).get());
    }
}

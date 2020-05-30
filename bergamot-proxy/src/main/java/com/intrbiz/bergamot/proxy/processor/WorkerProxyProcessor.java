package com.intrbiz.bergamot.proxy.processor;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.SiteMO;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.LookupAgentKey;
import com.intrbiz.bergamot.model.message.processor.proxy.LookupProxyKey;
import com.intrbiz.bergamot.model.message.proxy.AgentState;
import com.intrbiz.bergamot.model.message.proxy.ProxyMessage;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;
import com.intrbiz.bergamot.model.message.worker.agent.FoundAgentKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.server.MessageProcessor;

import io.netty.channel.Channel;

public class WorkerProxyProcessor extends MessageProcessor
{
    private static final Logger logger = Logger.getLogger(WorkerProxyProcessor.class);
    
    private final WorkerConsumer consumer;
    
    private final ProxyClient proxyClient;
    
    public WorkerProxyProcessor(ClientHeader client, Channel channel, WorkerConsumer consumer, ProxyClient proxyClient)
    {
        super(consumer.getId(), client, channel);
        this.consumer = consumer;
        this.proxyClient = proxyClient;
    }
    
    @Override
    public void start()
    {
        // start consuming messages
        this.consumer.start(this.channel.eventLoop(), this::forwardToRemoteworker);
    }
    
    @Override
    public void processMessage(Message message)
    {
        // Dispatch incoming messages to the processor
        if (message instanceof ProcessorMessage)
        {
            this.forwardProcessorMessageToCluster((ProcessorMessage) message);
        }
        else if (message instanceof ProxyMessage)
        {
            this.forwardProxyMessageToCluster((ProxyMessage) message);
        }
    }
    
    protected void forwardToRemoteworker(WorkerMessage message)
    {
        if (this.checkSiteId(message.getSiteId()))
        {
            // send the message to the remote worker
            this.channel.writeAndFlush(message);
        }
        else
        {
            logger.warn("Not forwarding worker message since it's not allowed for remote worker, siteId: " + message.getSiteId() + "\nClient: " + this.client);
        }
    }
    
    protected void forwardProcessorMessageToCluster(ProcessorMessage message)
    {
        // Filter and modify certain messages
        if (message instanceof LookupProxyKey)
        {
            // A proxy client cannot access proxy keys
            this.handleSecurityViolation("Proxy client attempted to lookup proxy key");
            return;
        }
        else if (message instanceof LookupAgentKey)
        {
            // This is because the proxy server owns the worker id and not the client
            ((LookupAgentKey) message).setWorkerId(this.getId());
            if (! (this.checkSiteId(message.getSiteId()) && this.checkSiteId(SiteMO.getSiteId(((LookupAgentKey) message).getKeyId()))))
            {
                // we're not allowed to lookup this agent key
                this.forwardToRemoteworker(new FoundAgentKey(((LookupAgentKey) message)));
                // drop this message
                return;
            }
        }
        // Filter messages to restrict to the authenticated site ids and forward the message
        if (this.validateSiteId(message.getSiteId()))
        {
            this.proxyClient.getProcessorDispatcher().dispatch(message);
        }
    }
    
    protected void forwardProxyMessageToCluster(ProxyMessage message)
    {
        if (message instanceof AgentState)
        {
            AgentState state = (AgentState) message;
            if (this.validateSiteId(state.getSiteId()))
            {
                try
                {                
                    // update the agent
                    if (state.isConnected())
                    {
                        this.proxyClient.registerAgent(state.getSiteId(), state.getAgentId(), state.getNonce(), this.getId());
                    }
                    else
                    {
                        this.proxyClient.unregisterAgent(state.getSiteId(), state.getAgentId(), state.getNonce(), this.getId());
                    }                
                }
                catch (Exception e)
                {
                    logger.warn("Failed to update agent state", e);
                }
            }
        }
    }
    
    @Override
    public void stop()
    {
        this.consumer.stop();
    }
}

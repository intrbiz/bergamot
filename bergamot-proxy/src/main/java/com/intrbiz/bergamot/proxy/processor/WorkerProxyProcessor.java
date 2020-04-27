package com.intrbiz.bergamot.proxy.processor;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.proxy.AgentState;
import com.intrbiz.bergamot.model.message.proxy.FoundAgentKey;
import com.intrbiz.bergamot.model.message.proxy.LookupAgentKey;
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
    
    public void start()
    {
        // start consuming messages
        this.consumer.start((message) -> {
            channel.writeAndFlush(message);
        });
    }

    @Override
    public void processMessage(Message message)
    {
        // Dispatch incoming messages to the processor
        if (message instanceof ProcessorMessage)
        {
            this.proxyClient.getProcessorDispatcher().dispatch((ProcessorMessage) message);
        }
        else if (message instanceof LookupAgentKey)
        {
            final LookupAgentKey lookup = (LookupAgentKey) message;
            this.proxyClient.getAgentKeyLookup().lookupAgentKey(lookup.getKeyId(), (key) -> {
                this.channel.writeAndFlush(new FoundAgentKey(lookup, key == null ? null : key.toString()));
            });
        }
        else if (message instanceof AgentState)
        {
            try
            {
                AgentState state = (AgentState) message;
                if (state.isConnected())
                {
                    this.proxyClient.registerAgent(state.getAgentId(), this.getId());
                }
                else
                {
                    this.proxyClient.unregisterAgent(state.getAgentId());
                }
            }
            catch (Exception e)
            {
                logger.warn("Failed to update agent state", e);
            }
        }
    }
    
    public void stop()
    {
        this.consumer.stop();
    }
}

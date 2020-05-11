package com.intrbiz.bergamot.proxy.processor;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.LookupAgentKey;
import com.intrbiz.bergamot.model.message.processor.proxy.LookupProxyKey;
import com.intrbiz.bergamot.model.message.proxy.AgentState;
import com.intrbiz.bergamot.model.message.proxy.ProxyMessage;
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
        this.consumer.start(this.channel.eventLoop(), (message) -> {
            channel.writeAndFlush(message);
        });
    }

    @Override
    public void processMessage(Message message)
    {
        // Dispatch incoming messages to the processor
        if (message instanceof ProcessorMessage)
        {
            this.processProcessorMessage((ProcessorMessage) message);
        }
        else if (message instanceof ProxyMessage)
        {
            this.processProxyMessage((ProxyMessage) message);
        }
    }
    
    protected void processProcessorMessage(ProcessorMessage message)
    {
        // Filter and modify certain messages
        if (message instanceof LookupProxyKey)
        {
            // A proxy client cannot access proxy keys
            return;
        }
        else if (message instanceof LookupAgentKey)
        {
            // This is because the proxy server owns the worker id and not the client
            ((LookupAgentKey) message).setWorkerId(this.getId());
        }
        // Forward the message
        this.proxyClient.getProcessorDispatcher().dispatch(message);
    }
    
    protected void processProxyMessage(ProxyMessage message)
    {
        if (message instanceof AgentState)
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

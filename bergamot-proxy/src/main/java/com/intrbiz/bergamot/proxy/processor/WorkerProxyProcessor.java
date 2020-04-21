package com.intrbiz.bergamot.proxy.processor;

import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.server.MessageProcessor;

import io.netty.channel.Channel;

public class WorkerProxyProcessor extends MessageProcessor
{
    private final WorkerConsumer consumer;
    
    private final ProcessorDispatcher processorDispatcher;
    
    public WorkerProxyProcessor(ClientHeader client, Channel channel, WorkerConsumer consumer, ProcessorDispatcher processorDispatcher)
    {
        super(consumer.getId(), client, channel);
        this.consumer = consumer;
        this.processorDispatcher = processorDispatcher;
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
            this.processorDispatcher.dispatch((ProcessorMessage) message);
        }
    }
    
    public void stop()
    {
        this.consumer.stop();
    }
}

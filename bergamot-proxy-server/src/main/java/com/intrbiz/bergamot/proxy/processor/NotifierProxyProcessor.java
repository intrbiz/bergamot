package com.intrbiz.bergamot.proxy.processor;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;

public class NotifierProxyProcessor extends MessageProcessor
{
    private final NotificationConsumer consumer;
    
    public NotifierProxyProcessor(ClientHeader client, Channel channel, NotificationConsumer consumer)
    {
        super(consumer.getId(), client, channel);
        this.consumer = consumer;
    }
    
    public void start()
    {
        // start consuming messages
        this.consumer.start(this.channel.eventLoop(),  (message) -> {
            channel.writeAndFlush(message);
        });
    }

    @Override
    public void processMessage(Message msg)
    {
    }
    
    public void stop()
    {
        this.consumer.stop();
    }
}

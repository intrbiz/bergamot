package com.intrbiz.bergamot.proxy.server;

import java.util.Objects;
import java.util.UUID;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;

public abstract class MessageProcessor
{
    protected final UUID id;
    
    protected final ClientHeader client;
    
    protected final Channel channel;
    
    public MessageProcessor(UUID id, ClientHeader client, Channel channel)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.client = Objects.requireNonNull(client);
        this.channel = Objects.requireNonNull(channel);
    }
    
    public UUID getId()
    {
        return this.id;
    }
    
    public abstract void start();
    
    public abstract void processMessage(Message msg);

    public abstract void stop();
    
    public static interface Factory
    {
        MessageProcessor create(ClientHeader client, Channel channel);
        
        void close(MessageProcessor processor);
    }
}

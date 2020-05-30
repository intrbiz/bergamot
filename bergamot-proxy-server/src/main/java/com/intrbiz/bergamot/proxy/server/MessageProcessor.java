package com.intrbiz.bergamot.proxy.server;

import java.util.Objects;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;

public abstract class MessageProcessor
{
    private static final Logger logger = Logger.getLogger(MessageProcessor.class);
    
    protected final UUID id;
    
    protected final ClientHeader client;
    
    protected final Channel channel;
    
    protected final UUID allowedSiteId;
    
    public MessageProcessor(UUID id, ClientHeader client, Channel channel)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.client = Objects.requireNonNull(client);
        this.channel = Objects.requireNonNull(channel);
        this.allowedSiteId = this.client.getAllowedSiteId();
    }
    
    public UUID getId()
    {
        return this.id;
    }
    
    public abstract void start();
    
    public abstract void processMessage(Message msg);

    public abstract void stop();
    
    protected boolean checkSiteId(UUID siteId)
    {
        return this.allowedSiteId == null || this.allowedSiteId.equals(siteId);
    }
    
    protected boolean validateSiteId(UUID siteId)
    {
        boolean allowed = checkSiteId(siteId);
        if (! allowed) this.handleSecurityViolation("Got bad site id from remote worker, siteId: " + siteId);
        return allowed;
    }
    
    protected void handleSecurityViolation(String message)
    {
        logger.warn("Got security violation: " + message + "\nClient: " + this.client);
        // trigger a close
        this.channel.close();
    }
    
    public static interface Factory
    {
        MessageProcessor create(ClientHeader client, Channel channel);
        
        void close(MessageProcessor processor);
    }
}

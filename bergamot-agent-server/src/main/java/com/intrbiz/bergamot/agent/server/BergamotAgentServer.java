package com.intrbiz.bergamot.agent.server;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import com.intrbiz.bergamot.io.BergamotAgentTranscoder;
import com.intrbiz.bergamot.proxy.BaseBergamotServer;
import com.intrbiz.bergamot.proxy.auth.KeyResolver;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.processor.MessageProcessor;

import io.netty.channel.Channel;

public class BergamotAgentServer extends BaseBergamotServer
{   
    private final ConcurrentMap<UUID, BergamotAgentHandler> agents = new ConcurrentHashMap<>();
    
    private volatile Consumer<BergamotAgentHandler> onAgentPing;
    
    private volatile Consumer<BergamotAgentHandler> onAgentConnect;
    
    private volatile Consumer<BergamotAgentHandler> onAgentDisconnect;

    public BergamotAgentServer(int port, KeyResolver keyResolver)
    {
        super(port, "Bergamot Agent", "/agent", BergamotAgentTranscoder.getDefault(), keyResolver);
    }

    @Override
    protected MessageProcessor create(ClientHeader client, Channel channel)
    {
        BergamotAgentHandler agent = new BergamotAgentHandler(client, channel, this::fireAgentPing, this::fireAgentConnect, this::fireAgentDisconnect);
        this.agents.put(agent.getAgentId(), agent);
        return agent;
    }

    @Override
    protected void close(MessageProcessor processor)
    {
        if (processor instanceof BergamotAgentHandler)
        {
            BergamotAgentHandler handler = (BergamotAgentHandler) processor;
            BergamotAgentHandler agent = this.agents.get(handler.getAgentId());
            if (agent != null && agent.getNonce().equals(handler.getNonce()))
            {
                this.agents.remove(handler.getAgentId(), agent);
            }
        }
    }

    public BergamotAgent getAgent(UUID id)
    {
        return this.agents.get(id);
    }
    
    public Collection<BergamotAgent> getAgents()
    {
        return Collections.unmodifiableCollection(this.agents.values());
    }
    
    // Event handlers
    
    public void setOnAgentPingHandler(Consumer<BergamotAgentHandler> onAgentPing)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentPing = this.onAgentPing == null ? onAgentPing : this.onAgentPing.andThen(onAgentPing);
        }
    }
    
    public Consumer<BergamotAgentHandler> getOnAgentPing()
    {
        return this.onAgentPing;
    }
    
    public void setOnAgentConnectHandler(Consumer<BergamotAgentHandler> onAgentConnect)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentConnect = this.onAgentConnect == null ? onAgentConnect : this.onAgentConnect.andThen(onAgentConnect);
        }
    }
    
    public Consumer<BergamotAgentHandler> getOnAgentConnect()
    {
        return this.onAgentConnect;
    }
    
    public void setOnAgentDisconnectHandler(Consumer<BergamotAgentHandler> onAgentDisconnect)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentDisconnect = this.onAgentDisconnect == null ? onAgentDisconnect : this.onAgentDisconnect.andThen(onAgentDisconnect);
        }
    }
    
    public Consumer<BergamotAgentHandler> getOnAgentDisconnect()
    {
        return this.onAgentDisconnect;
    }
    
    // Event Handlers
    
    public void fireAgentPing(BergamotAgentHandler agent)
    {
        // fire the agent ping hook
        if (this.onAgentPing != null) this.onAgentPing.accept(agent);
    }
    
    public void fireAgentConnect(BergamotAgentHandler agent)
    {
        // fire the agent ping hook
        if (this.onAgentConnect != null) this.onAgentConnect.accept(agent);
    }
    
    public void fireAgentDisconnect(BergamotAgentHandler agent)
    {
        // fire the agent ping hook
        if (this.onAgentDisconnect != null) this.onAgentDisconnect.accept(agent);
    }
}

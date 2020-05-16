package com.intrbiz.bergamot.cluster.client.proxy;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.BergamotConfig;
import com.intrbiz.bergamot.cluster.client.BergamotClient;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.client.BergamotProxyClient;
import com.intrbiz.bergamot.proxy.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.util.HostUtil;

import io.netty.channel.Channel;

public abstract class ProxyBaseClient implements BergamotClient
{
    private static Logger logger = Logger.getLogger(ProxyBaseClient.class);
    
    protected final UUID id;
    
    protected final String hostName;
    
    protected final Consumer<Void> onPanic;
    
    protected final BergamotProxyClient client;
    
    protected final Channel channel;
    
    protected final AtomicBoolean paniced = new AtomicBoolean(false);
    
    public ProxyBaseClient(Consumer<Void> onPanic, ClientHeader headers) throws Exception
    {
        super();
        this.onPanic = Objects.requireNonNull(onPanic);
        this.id = UUID.randomUUID();
        this.hostName = this.getHostName();
        this.client = new BergamotProxyClient(new URI(Objects.requireNonNull(BergamotConfig.getProxyUrl(), "The proxy URL must be given")));
        // Connect
        this.channel = this.client.connect(headers.hostName(this.hostName), new AuthenticationKey(Objects.requireNonNull(BergamotConfig.getProxyKey(), "The proxy key must be given")), this::handleMessage).sync().get();
        this.channel.closeFuture().addListener((future) -> {
            logger.info("Proxy connection closed");
            this.panic();
        });
    }
    
    protected String getHostName()
    {
        return HostUtil.getHostName();
    }
    
    protected void panic()
    {
        if (this.paniced.compareAndSet(false, true))
        {
            logger.fatal("PANIC - lost connection to cluster");
            this.onPanic.accept(null);
        }
    }
    
    protected abstract void handleMessage(Message message);

    @Override
    public UUID getId()
    {
        return this.id;
    }

    @Override
    public void close()
    {
        this.paniced.set(true);
        this.channel.close();
        this.client.stop();
    }
}

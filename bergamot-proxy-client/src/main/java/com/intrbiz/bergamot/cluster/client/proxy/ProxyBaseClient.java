package com.intrbiz.bergamot.cluster.client.proxy;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.client.BergamotClient;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.client.BergamotProxyClient;
import com.intrbiz.bergamot.proxy.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;

public abstract class ProxyBaseClient implements BergamotClient
{
    private static Logger logger = Logger.getLogger(ProxyBaseClient.class);
    
    protected final ClusterCfg config;
    
    protected final UUID id;
    
    protected final String hostName;
    
    protected final Consumer<Void> onPanic;
    
    protected final BergamotProxyClient client;
    
    protected final Channel channel;
    
    protected final AtomicBoolean paniced = new AtomicBoolean(false);
    
    public ProxyBaseClient(ClusterCfg config, Consumer<Void> onPanic, ClientHeader headers) throws Exception
    {
        super();
        this.config = Objects.requireNonNull(config);
        this.onPanic = Objects.requireNonNull(onPanic);
        this.id = UUID.randomUUID();
        this.hostName = this.getHostName();
        this.client = new BergamotProxyClient(new URI(getProxyUrl(this.config)));
        // Connect
        this.channel = this.client.connect(headers.hostName(this.hostName), new AuthenticationKey(getProxyKey(this.config)), this::handleMessage).sync().get();
        this.channel.closeFuture().addListener((future) -> {
            logger.info("Proxy connection closed");
            this.panic();
        });
    }
    
    public static String getProxyUrl(ClusterCfg config)
    {
        return Util.coalesceEmpty(
            System.getenv("PROXY_URL"), 
            System.getProperty("proxy.url"), 
            config.getProxyUrl()
        );
    }
    
    public static String getProxyKey(ClusterCfg config)
    {
        return Util.coalesceEmpty(
            System.getenv("PROXY_Key"), 
            System.getProperty("proxy.key"), 
            config.getProxyKey()
        );
    }
    
    protected String getHostName()
    {
        try
        {
            return Util.coalesceEmpty(System.getenv("BERGAMOT_HOSTNAME"), System.getProperty("bergamot.host.name"), InetAddress.getLocalHost().getHostName());
        }
        catch (UnknownHostException e)
        {
            logger.warn("Failed to get node host name", e);
        }
        return null;
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

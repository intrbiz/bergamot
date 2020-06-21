package com.intrbiz.bergamot.proxy.server;

import java.util.Objects;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.io.BergamotCoreTranscoder;
import com.intrbiz.bergamot.proxy.BaseBergamotServer;
import com.intrbiz.bergamot.proxy.auth.KeyResolver;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.processor.MessageProcessor;
import com.intrbiz.bergamot.proxy.processor.NotifierProxyProcessor;
import com.intrbiz.bergamot.proxy.processor.WorkerProxyProcessor;

import io.netty.channel.Channel;

public class BergamotProxyServer extends BaseBergamotServer
{
    private static final Logger logger = Logger.getLogger(BergamotProxyServer.class);
    
    private ProxyClient client;
    
    public BergamotProxyServer(int port, KeyResolver keyResolver, ProxyClient client)
    {
        super(port, "Bergamot Proxy", "/proxy", BergamotCoreTranscoder.getDefault(), keyResolver);
        this.client = Objects.requireNonNull(client);
    }
    
    @Override
    protected MessageProcessor create(ClientHeader clientHeaders, Channel channel)
    {
        // What type proxy client is this
        String proxyFor = clientHeaders.getProxyFor();
        if (ClientHeader.BergamotHeaderValues.PROXY_FOR_WORKER.equals(proxyFor))
        {
            // create a worker proxy
            try
            {
                return new WorkerProxyProcessor(
                    clientHeaders, 
                    channel,
                    client.registerWorker(clientHeaders.getHostName(), clientHeaders.getUserAgent(), clientHeaders.getInfo(), clientHeaders.getAllowedSiteIds(), clientHeaders.getWorkerPool(), clientHeaders.getEngines()),
                    client
                );
            }
            catch (Exception e)
            {
                logger.error("Failed to register proxy worker", e);
            }
        }
        else if (ClientHeader.BergamotHeaderValues.PROXY_FOR_NOTIFIER.equals(proxyFor))
        {
            // create a notifier proxy
            try
            {
                return new NotifierProxyProcessor(
                    clientHeaders, 
                    channel, 
                    client.registerNotifier(clientHeaders.getHostName(), clientHeaders.getUserAgent(), clientHeaders.getInfo(), clientHeaders.getAllowedSiteIds(), clientHeaders.getEngines())
                );
            }
            catch (Exception e)
            {
                logger.error("Failed to register proxy notifier", e);
            }
        }
        return null;
    }
    
    @Override
    protected void close(MessageProcessor processor)
    {
        // TODO: we should retry unregister in the event of an error
        if (processor instanceof WorkerProxyProcessor)
        {
            try
            {
                client.unregisterWorker(processor.getId());
            }
            catch (Exception e)
            {
                logger.error("Failed to unregister proxy worker", e);
            }
        }
        else if (processor instanceof NotifierProxyProcessor)
        {
            
            try
            {
                client.unregisterNotifier(processor.getId());
            }
            catch (Exception e)
            {
                logger.error("Failed to unregister proxy notifier", e);
            }
        }
    }
}

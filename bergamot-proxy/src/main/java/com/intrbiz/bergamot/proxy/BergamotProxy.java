package com.intrbiz.bergamot.proxy;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.BergamotConfig;
import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.client.hz.HZProxyClient;
import com.intrbiz.bergamot.model.message.processor.proxy.LookupProxyKey;
import com.intrbiz.bergamot.model.message.proxy.FoundProxyKey;
import com.intrbiz.bergamot.model.message.proxy.ProxyMessage;
import com.intrbiz.bergamot.proxy.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.processor.NotifierProxyProcessor;
import com.intrbiz.bergamot.proxy.processor.WorkerProxyProcessor;
import com.intrbiz.bergamot.proxy.server.BergamotProxyServer;
import com.intrbiz.bergamot.proxy.server.MessageProcessor;

import io.netty.channel.Channel;

public class BergamotProxy
{
    private static final Logger logger = Logger.getLogger(BergamotProxy.class);
    
    private int port;
    
    private ProxyClient client;
    
    private BergamotProxyServer server;
    
    private volatile CountDownLatch shutdownLatch;
    
    private final ConcurrentMap<UUID, BiConsumer<AuthenticationKey, UUID>> proxyKeyLookups = new ConcurrentHashMap<>();
    
    public BergamotProxy()
    {
        super();
    }
    
    public final void configure() throws Exception
    {
        this.port = BergamotConfig.getProxyPort();
    }
    
    protected void resolveKey(UUID keyId, BiConsumer<AuthenticationKey, UUID> callback)
    {
        // TODO: cache keys
        // fire off the lookup
        LookupProxyKey lookup = new LookupProxyKey(keyId, this.client.getId());
        this.proxyKeyLookups.put(lookup.getId(), callback);
        this.client.getProcessorDispatcher().dispatch(lookup);
    }
    
    protected MessageProcessor.Factory createMessageProcessorFactory()
    {
        return new MessageProcessor.Factory()
        {
            @Override
            public MessageProcessor create(ClientHeader clientHeaders, Channel channel)
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
                            client.registerWorker(clientHeaders.getHostName(), clientHeaders.getUserAgent(), clientHeaders.getInfo(), clientHeaders.getSiteIds(), clientHeaders.getWorkerPool(), clientHeaders.getEngines()),
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
                            client.registerNotifier(clientHeaders.getHostName(), clientHeaders.getUserAgent(), clientHeaders.getInfo(), clientHeaders.getSiteIds(), clientHeaders.getEngines())
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
            public void close(MessageProcessor processor)
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
        };
    }

    protected void start() throws Exception
    {
        logger.info("Bergamot Proxy starting....");
        this.shutdownLatch = new CountDownLatch(1);
        // connect to the scheduler
        logger.info("Connecting to cluster");
        this.client = new HZProxyClient(this::clusterPanic, this.getClass().getSimpleName(), BergamotVersion.fullVersionString());
        // start our server
        this.server = new BergamotProxyServer(this.port, this::resolveKey, this.createMessageProcessorFactory());
        this.server.start();
        // start consuming our messages
        this.startConsuming();
        logger.info("Bergamot Proxy started.");
    }
    
    protected void startConsuming() throws Exception
    {
        logger.info("Starting consuming proxy messages");
        this.client.getProxyConsumer().start(this.server.getServerExecutor(), this::processProxyMessage);
    }
    
    protected void processProxyMessage(ProxyMessage message)
    {
        if (message instanceof FoundProxyKey)
        {
            BiConsumer<AuthenticationKey, UUID> callback = this.proxyKeyLookups.remove(message.getReplyTo());
            if (callback != null)
            {
                FoundProxyKey found = (FoundProxyKey) message;
                callback.accept(found.getKey() == null ? null : new AuthenticationKey(found.getKey()), found.getSiteId());
            }
        }
    }
    
    protected void stopConsuming()
    {
        logger.info("Stopping consuming proxy messages");
        this.client.getProxyConsumer().stop();
    }
    
    protected void stop()
    {
        logger.info("Bergamot Proxy stopping....");
        // Stop consuming
        this.stopConsuming();
        // stop our server
        this.server.stop();
        // disconnect from the scheduler
        logger.info("Disconnecting from cluster");
        this.client.close();
        logger.info("Bergamot Proxy stopped.");
    }
    
    protected void clusterPanic(Void v)
    {
        logger.fatal("Connection to cluster lost, forcing shutdown now!");
        this.triggerShutdown();
    }
    
    public void awaitShutdown()
    {
        try
        {
            this.shutdownLatch.await();
        }
        catch (InterruptedException e)
        {
        }
    }
    
    public void run() throws Exception
    {
        // Start
        this.start();
        // Wait for shutdown
        this.awaitShutdown();
        // Stop
        this.stop();
    }
    
    public void triggerShutdown()
    {
        logger.info("Triggering shutdown of " + this.getClass().getSimpleName());
        this.shutdownLatch.countDown();
    }
    
    /**
     * Start this proxy daemon
     */
    public static void main(String[] args) throws Exception
    {
        try
        {
            // Configure logging
            BergamotConfig.configureLogging();
            // Create the worker
            BergamotProxy proxy = new BergamotProxy();
            proxy.configure();
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(proxy::triggerShutdown));
            // Run our proxy
            proxy.run();
            // Terminate normally
            Thread.sleep(15_000);
            System.exit(0);
        }
        catch (Exception e)
        {
            System.err.println("Failed to start Bergamot Proxy!");
            e.printStackTrace();
            Thread.sleep(15_000);
            System.exit(1);
        }
    }
}

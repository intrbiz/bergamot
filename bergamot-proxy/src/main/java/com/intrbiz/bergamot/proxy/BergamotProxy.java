package com.intrbiz.bergamot.proxy;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.BergamotConfig;
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.client.hz.HZProxyClient;
import com.intrbiz.bergamot.model.AuthenticationKey;
import com.intrbiz.bergamot.model.message.processor.proxy.LookupProxyKey;
import com.intrbiz.bergamot.model.message.proxy.FoundProxyKey;
import com.intrbiz.bergamot.model.message.proxy.ProxyMessage;
import com.intrbiz.bergamot.proxy.server.BergamotProxyServer;

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
    
    protected void start() throws Exception
    {
        logger.info("Bergamot Proxy starting....");
        this.shutdownLatch = new CountDownLatch(1);
        // connect to the scheduler
        logger.info("Connecting to cluster");
        this.client = new HZProxyClient(this::clusterPanic, this.getClass().getSimpleName(), BergamotVersion.fullVersionString());
        // start our server
        this.server = new BergamotProxyServer(this.port, this::resolveKey, this.client);
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

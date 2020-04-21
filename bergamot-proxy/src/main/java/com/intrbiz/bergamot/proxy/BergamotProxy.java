package com.intrbiz.bergamot.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.zookeeper.KeeperException;

import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.client.hz.HZProxyClient;
import com.intrbiz.bergamot.config.LoggingCfg;
import com.intrbiz.bergamot.config.ProxyCfg;
import com.intrbiz.bergamot.proxy.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.processor.NotifierProxyProcessor;
import com.intrbiz.bergamot.proxy.processor.WorkerProxyProcessor;
import com.intrbiz.bergamot.proxy.server.BergamotProxyServer;
import com.intrbiz.bergamot.proxy.server.MessageProcessor;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.configuration.Configuration;

import io.netty.channel.Channel;

/**
 * A worker to execute nagios check engines
 */
public class BergamotProxy implements Configurable<ProxyCfg>
{   
    public static final String DEFAULT_CONFIGURATION_FILE = "/etc/bergamot/proxy/default.xml";
    
    public static final String DAEMON_NAME = "bergamot-proxy";
    
    private static final Logger logger = Logger.getLogger(BergamotProxy.class);
    
    private ProxyCfg configuration;
    
    private int port;
    
    private ProxyClient client;
    
    private BergamotProxyServer server;
    
    public BergamotProxy()
    {
        super();
    }

    public final ProxyCfg getConfiguration()
    {
        return this.configuration;
    }
    
    protected String getConfigurationParameter(String name, Supplier<String> cfgValue, String defaultValue)
    {
        /*
         * Fetch configuration in order of:
         *  - Env var
         *  - System property
         *  - Configuration parameter
         *  - Specific configuration method
         *  - Default value
         */
        return Util.coalesceEmpty(
            System.getenv(name.toUpperCase().replace('.', '_').replace('-', '_')),
            System.getProperty(name),
            configuration.getStringParameterValue(name, null),
            cfgValue == null ? null : cfgValue.get(),
            defaultValue
        );
    }
    
    @Override
    public final void configure(ProxyCfg cfg) throws Exception
    {
        this.configuration = cfg;
        this.port = Integer.parseInt(this.getConfigurationParameter("proxy.port", () -> String.valueOf(this.configuration.getPort()), "14080"));
    }
    
    protected CompletionStage<AuthenticationKey> resolveKey(UUID keyId)
    {
        return this.client.getProxyKeyLookup().lookupProxyKey(keyId)
                .thenApply((key) -> key == null || key.isRevoked()? null : key.toAuthenticationKey());
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
                            client.registerWorker(DAEMON_NAME, clientHeaders.getUserAgent(), clientHeaders.getSiteIds(), clientHeaders.getWorkerPool(), clientHeaders.getEngines()),
                            client.getProcessorDispatcher()
                        );
                    }
                    catch (KeeperException | InterruptedException e)
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
                            client.registerNotifier(DAEMON_NAME, clientHeaders.getUserAgent(), clientHeaders.getSiteIds(), clientHeaders.getEngines())
                        );
                    }
                    catch (KeeperException | InterruptedException e)
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
                    catch (KeeperException | InterruptedException e)
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
                    catch (KeeperException | InterruptedException e)
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
        // connect to the scheduler
        logger.info("Connecting to cluster");
        this.client = new HZProxyClient(this.configuration.getCluster(), this::clusterPanic);
        // start our server
        this.server = new BergamotProxyServer(this.port, this::resolveKey, this.createMessageProcessorFactory());
        this.server.start();
        logger.info("Bergamot Proxy started.");
    }
    
    protected void stop()
    {
        logger.info("Bergamot Proxy stopping....");
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
        this.stop();
    }
    
    /**
     * Start this proxy daemon
     */
    public static void main(String[] args) throws Exception
    {
        try
        {
            // Load configuration
            ProxyCfg config = loadConfiguration();
            // Configure logging
            configureLogging(config.getLogging());
            Logger logger = Logger.getLogger(BergamotProxy.class);
            logger.info("Bergamot Proxy, using configuration:\r\n" + config.toString());
            // Create the worker
            BergamotProxy proxy = new BergamotProxy();
            proxy.configure(config);
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Triggering shutdown of Bergamot Proxy");
                proxy.stop();
            }));
            // Start our proxy
            proxy.start();
        }
        catch (Exception e)
        {
            System.err.println("Failed to start Bergamot Proxy!");
            e.printStackTrace();
            Thread.sleep(15_000);
            System.exit(1);
        }
    }
    
    /**
     * Search for the configuration file
     */
    private static File getConfigurationFile()
    {
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("BERGAMOT_CONFIG"), DEFAULT_CONFIGURATION_FILE));
    }
    
    private static ProxyCfg loadConfiguration() throws Exception
    {
        ProxyCfg config = null;
        // try the config file?
        File configFile = getConfigurationFile();
        if (configFile.exists())
        {
            System.out.println("Using configuration file " + configFile.getAbsolutePath());
            config = Configuration.read(ProxyCfg.class, new FileInputStream(configFile));
        }
        else
        {
            config = new ProxyCfg();
        }
        config.applyDefaults();
        return config;
    }
    
    private static void configureLogging(LoggingCfg config) throws Exception
    {
        if (config == null) config = new LoggingCfg();
        // configure logging to terminal
        Logger root = Logger.getRootLogger();
        root.addAppender(new ConsoleAppender(new PatternLayout("%d [%t] %p %c %x - %m%n")));
        root.setLevel(Level.toLevel(config.getLevel().toUpperCase()));
    }
}

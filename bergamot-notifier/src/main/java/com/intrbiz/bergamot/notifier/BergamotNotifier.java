package com.intrbiz.bergamot.notifier;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.intrbiz.Util;
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.cluster.client.NotifierClient;
import com.intrbiz.bergamot.cluster.client.hz.HZNotifierClient;
import com.intrbiz.bergamot.cluster.client.proxy.ProxyBaseClient;
import com.intrbiz.bergamot.cluster.client.proxy.ProxyNotifierClient;
import com.intrbiz.bergamot.config.LoggingCfg;
import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.NotificationEngine;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.util.IBThreadFactory;
import com.intrbiz.util.IntrbizBootstrap;

public class BergamotNotifier implements Configurable<NotifierCfg>
{   
    public static final String DEFAULT_CONFIGURATION_FILE = "/etc/bergamot/notifier/default.xml";
    
    public static final String DAEMON_NAME = "bergamot-notifier";
    
    private static final Logger logger = Logger.getLogger(BergamotNotifier.class);
    
    private NotifierCfg configuration;
 
    private Set<String> enabledEngines = new HashSet<>();
    
    private Set<String> disabledEngines = new HashSet<>();
    
    private Map<String, NotificationEngine> engines = new TreeMap<String, NotificationEngine>();

    private Set<UUID> sites = new HashSet<>();
    
    private int threadCount;
    
    private NotifierClient client;
    
    private ExecutorService executor;
    
    private volatile CountDownLatch shutdownLatch;
    
    public BergamotNotifier()
    {
        super();
    }

    public final NotifierCfg getConfiguration()
    {
        return this.configuration;
    }

    public final Set<UUID> getSites()
    {
        return this.sites;
    }

    public final Collection<NotificationEngine> getEngines()
    {
        return this.engines.values();
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
        return Util.coalesce(
                Util.coalesceEmpty(
                    System.getenv(name.toUpperCase().replace('.', '_').replace('-', '_')),
                    System.getProperty(name),
                    configuration.getStringParameterValue(name, null),
                    cfgValue == null ? null : cfgValue.get()
                ),
                defaultValue
        );
    }
    
    protected String getConfigurationParameter(String name, String defaultValue)
    {
        return this.getConfigurationParameter(name, null, defaultValue);
    }
    
    protected boolean isEngineEnabled(String engineName, boolean enabledByDefault)
    {
        if (this.enabledEngines.contains(engineName))
            return true;
        if (this.disabledEngines.contains(engineName))
            return false;
        return enabledByDefault;
    }
    
    @Override
    public final void configure(NotifierCfg cfg) throws Exception
    {
        this.configuration = cfg;
        // TODO: support multiple sites
        String site = this.getConfigurationParameter("site", this.configuration::getSite, null);
        if (! Util.isEmpty(site))
        {
            this.sites.add(UUID.fromString(site));
        }
        this.threadCount = Integer.parseInt(this.getConfigurationParameter("threads", this.configuration::getThreads, String.valueOf(Runtime.getRuntime().availableProcessors())));
        this.enabledEngines = Arrays.stream(this.getConfigurationParameter("enabled.engines", "").split(",")).map(String::trim).collect(Collectors.toSet());
        this.disabledEngines = Arrays.stream(this.getConfigurationParameter("disabled.engines", "").split(",")).map(String::trim).collect(Collectors.toSet());
         // register engines
        Set<Class<?>> processed = new HashSet<>();
        for (NotificationEngine availableEngine : ServiceLoader.load(NotificationEngine.class))
        {
            this.registerEngine(availableEngine, processed);
        }
        for (URLClassLoader pluginLoader : IntrbizBootstrap.getPluginClassLoaders())
        {
            for (NotificationEngine availableEngine : ServiceLoader.load(NotificationEngine.class, pluginLoader))
            {
                this.registerEngine(availableEngine,processed);
            }    
        }
    }
    
    protected void registerEngine(NotificationEngine availableEngine, Set<Class<?>> processed)
    {
        if (! processed.contains(availableEngine.getClass()))
        {
            if (this.isEngineEnabled(availableEngine.getName(), availableEngine.isEnabledByDefault()))
            {
                logger.info("Registering check engine: " + availableEngine);
                this.engines.put(availableEngine.getName(), availableEngine);
            }
            else
            {
                logger.info("Skipping check engine: " + availableEngine);   
            }
        }
        processed.add(availableEngine.getClass());
    }

    public void start() throws Exception
    {
        logger.info("Bergamot Notifier starting....");
        this.shutdownLatch = new CountDownLatch(1);
        // prepare our engines
        this.prepareEngines();
        // prepare our executors
        this.createExecutor();
        // connect to the scheduler
        this.connectCluster();
        // start our engines
        this.startEngines();
        // start our executors
        this.startConsuming();
    }
    
    protected void prepareEngines() throws Exception
    {
        for (NotificationEngine engine : this.getEngines())
        {
            logger.info("Preparing notification engine: " + engine.getName());
            engine.prepare(this.createEngineContext(engine));
        }
    }

    protected NotificationEngineContext createEngineContext(final NotificationEngine e)
    {
        return new NotificationEngineContext() {
            @Override
            public String getParameter(String name, String defaultValue)
            {
                return getConfigurationParameter(name, defaultValue);
            }
        };
    }
    
    protected void connectCluster() throws Exception
    {
        if (! Util.isEmpty(ProxyBaseClient.getProxyUrl(this.configuration.getCluster())))
        {
            logger.info("Connecting to proxy");
            this.client = new ProxyNotifierClient(this.configuration.getCluster(), this::clusterPanic, DAEMON_NAME, BergamotVersion.fullVersionString(), this.engines.keySet());
        }
        else
        {
            logger.info("Connecting to cluster");
            this.client = new HZNotifierClient(this.configuration.getCluster(), this::clusterPanic, DAEMON_NAME, BergamotVersion.fullVersionString(), this.sites, this.engines.keySet());
        }
    }
    
    protected void clusterPanic(Void v)
    {
        logger.fatal("Connection to cluster lost, forcing shutdown now!");
        // Trigger a shutdown
        this.triggerShutdown();
    }
    
    protected void createExecutor() throws Exception
    {
        logger.info("Creating " + this.threadCount + " notification executors");
        this.executor = Executors.newFixedThreadPool(this.threadCount, new IBThreadFactory("bergamot-notifier-executor", true));
    }
    
    protected void startEngines() throws Exception
    {
        for (NotificationEngine engine : this.getEngines())
        {
            logger.info("Starting notification engine: " + engine.getName());
            engine.start(this.createEngineContext(engine));
        }
    }
    
    protected void startConsuming() throws Exception
    {
        logger.info("Starting consuming notifications");
        this.client.getNotifierConsumer().start(this.executor, this::sendNotification);
    }

    protected void sendNotification(Notification notification)
    {
        if (notification != null)
        {
            if (logger.isTraceEnabled()) logger.trace("Sending notification: " + notification);
            try
            {
             // send the notification for the requested engine
                NotificationEngine engine = this.engines.get(notification.getEngine());
                if (engine != null && engine.accept(notification))
                {
                    engine.sendNotification(notification);
                }
            }
            catch (Exception e)
            {
                logger.error("Error sending notification", e);
                // TODO: We should ack notifications or similar
            }
        }
    }
    
    protected void stopConsuming()
    {
        logger.info("Stopping consuming notifications");
        this.client.getNotifierConsumer().stop();
    }

    protected void shutdownEngines()
    {
        for (NotificationEngine engine : this.getEngines())
        {
            logger.info("Shutting down notification engine: " + engine.getName());
            engine.shutdown(this.createEngineContext(engine));
        }
    }

    protected void shutdownExecutor()
    {
        this.executor.shutdown();
        try
        {
            this.executor.awaitTermination(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }
    }

    protected void disconnectScheduler()
    {
        logger.info("Disconnecting from cluster");
        this.client.close();
    }

    protected void stop()
    {
        logger.info("Bergamot Notifier stopping....");
        // Stop consuming
        this.stopConsuming();
        // Shutdown all engines
        this.shutdownEngines();
        // Shutdown executors
        this.shutdownExecutor();
        // Disconnect from the scheduler
        this.disconnectScheduler();
        logger.info("Bergamot Notifier stopped.");
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
     * Start this worker daemon
     */
    public static void main(String[] args) throws Exception
    {
        try
        {
            // Load configuration
            NotifierCfg config = loadConfiguration();
            // Configure logging
            configureLogging(config.getLogging());
            Logger logger = Logger.getLogger(BergamotNotifier.class);
            logger.info("Bergamot Notifier, using configuration:\r\n" + config.toString());
            // Create the worker
            BergamotNotifier notifier = new BergamotNotifier();
            notifier.configure(config);
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(notifier::triggerShutdown));
            // Run our notifier
            notifier.run();
            // Terminate normally
            Thread.sleep(15_000);
            System.exit(0);
        }
        catch (Exception e)
        {
            System.err.println("Failed to start Bergamot Notifier!");
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
    
    private static NotifierCfg loadConfiguration() throws Exception
    {
        NotifierCfg config = null;
        // try the config file?
        File configFile = getConfigurationFile();
        if (configFile.exists())
        {
            System.out.println("Using configuration file " + configFile.getAbsolutePath());
            config = Configuration.read(NotifierCfg.class, new FileInputStream(configFile));
        }
        else
        {
            config = new NotifierCfg();
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

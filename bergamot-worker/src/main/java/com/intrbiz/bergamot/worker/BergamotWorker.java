package com.intrbiz.bergamot.worker;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.intrbiz.Util;
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.cluster.client.WorkerClient;
import com.intrbiz.bergamot.cluster.client.hz.HZWorkerClient;
import com.intrbiz.bergamot.cluster.client.proxy.ProxyBaseClient;
import com.intrbiz.bergamot.cluster.client.proxy.ProxyWorkerClient;
import com.intrbiz.bergamot.config.LoggingCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.processor.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.Engine;
import com.intrbiz.bergamot.worker.engine.EngineContext;
import com.intrbiz.bergamot.worker.engine.agent.AgentEngine;
import com.intrbiz.bergamot.worker.engine.dummy.DummyEngine;
import com.intrbiz.bergamot.worker.engine.http.HTTPEngine;
import com.intrbiz.bergamot.worker.engine.jdbc.JDBCEngine;
import com.intrbiz.bergamot.worker.engine.jmx.JMXEngine;
import com.intrbiz.bergamot.worker.engine.nagios.NagiosEngine;
import com.intrbiz.bergamot.worker.engine.nrpe.NRPEEngine;
import com.intrbiz.bergamot.worker.engine.sftp.SFTPEngine;
import com.intrbiz.bergamot.worker.engine.snmp.SNMPEngine;
import com.intrbiz.bergamot.worker.engine.ssh.SSHEngine;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.util.IBThreadFactory;

/**
 * A worker to execute nagios check engines
 */
public class BergamotWorker implements Configurable<WorkerCfg>
{
    public static final String DEFAULT_CONFIGURATION_FILE = "/etc/bergamot/worker/default.xml";

    public static final String DAEMON_NAME = "bergamot-worker";

    private static final Logger logger = Logger.getLogger(BergamotWorker.class);

    private static final List<AvailableEngine> AVAILABLE_ENGINES = new ArrayList<>();

    static
    {
        // register all engines which are available
        AVAILABLE_ENGINES.add(new AvailableEngine(DummyEngine.NAME, DummyEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(NagiosEngine.NAME, NagiosEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(NRPEEngine.NAME, NRPEEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(HTTPEngine.NAME, HTTPEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(JDBCEngine.NAME, JDBCEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(JMXEngine.NAME, JMXEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(SSHEngine.NAME, SSHEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(SFTPEngine.NAME, SFTPEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(SNMPEngine.NAME, SNMPEngine::new, false));
        AVAILABLE_ENGINES.add(new AvailableEngine(AgentEngine.NAME, AgentEngine::new, false));
    }

    private WorkerCfg configuration;

    private Map<String, Engine> engines = new TreeMap<String, Engine>();

    private Set<UUID> sites = new HashSet<>();

    private String workerPool;

    private int threadCount;

    private WorkerClient client;

    private ExecutorService executor;

    private CountDownLatch shutdownLatch;

    public BergamotWorker()
    {
        super();
    }

    public final WorkerCfg getConfiguration()
    {
        return this.configuration;
    }

    public final Set<UUID> getSites()
    {
        return this.sites;
    }

    public final String getWorkerPool()
    {
        return this.workerPool;
    }

    public final Collection<Engine> getEngines()
    {
        return this.engines.values();
    }

    protected String getConfigurationParameter(String name, Supplier<String> cfgValue, String defaultValue)
    {
        /*
         * Fetch configuration in order of: - Env var - System property - Configuration parameter - Specific configuration method - Default value
         */
        return Util.coalesceEmpty(System.getenv(name.toUpperCase().replace('.', '_').replace('-', '_')), System.getProperty(name), configuration.getStringParameterValue(name, null), cfgValue == null ? null : cfgValue.get(), defaultValue);
    }

    protected String getConfigurationParameter(String name, String defaultValue)
    {
        return this.getConfigurationParameter(name, null, defaultValue);
    }

    @Override
    public final void configure(WorkerCfg cfg) throws Exception
    {
        this.configuration = cfg;
        // TODO: support multiple sites
        String site = this.getConfigurationParameter("site", this.configuration::getSite, null);
        if (!Util.isEmpty(site))
        {
            this.sites.add(UUID.fromString(site));
        }
        this.workerPool = this.getConfigurationParameter("worker-pool", this.configuration::getWorkerPool, null);
        this.threadCount = Integer.parseInt(this.getConfigurationParameter("threads", String.valueOf(this.configuration.getThreads())));
        // register engines
        for (AvailableEngine availableEngine : AVAILABLE_ENGINES)
        {
            if (this.configuration.isEngineEnabled(availableEngine.name, availableEngine.enabledByDefault))
            {
                Engine engine = availableEngine.constructor.get();
                this.engines.put(engine.getName(), engine);
                logger.info("Registering check engine: " + engine.getName());
            }
        }
    }

    protected void start() throws Exception
    {
        logger.info("Bergamot Worker starting....");
        this.shutdownLatch = new CountDownLatch(1);
        // prepare our engines
        this.prepareEngines();
        // prepare our executors
        this.createExecutor();
        // connect to the scheduler
        this.connectCluster();
        // start our engines
        this.startEngines();
        // start consuming checks
        this.startConsuming();
        logger.info("Bergamot Worker started.");
    }

    protected void prepareEngines() throws Exception
    {
        for (Engine engine : this.getEngines())
        {
            logger.info("Preparing check engine: " + engine.getName());
            engine.prepare(this.createEngineContext(engine));
        }
    }

    protected EngineContext createEngineContext(final Engine e)
    {
        return new EngineContext()
        {
            @Override
            public String getParameter(String name, String defaultValue)
            {
                return getConfigurationParameter(name, defaultValue);
            }

            @Override
            public void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback)
            {
                client.getAgentKeyLookup().lookupAgentKey(keyId, callback);
            }

            @Override
            public void publishAgentAction(AgentMessage event)
            {
                client.getProcessorDispatcher().dispatchAgentMessage(event);
            }

            @Override
            public void registerAgent(UUID agentId)
            {
                try
                {
                    client.registerAgent(agentId);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Failed to register agent", e);
                }
            }

            @Override
            public void unregisterAgent(UUID agentId)
            {
                try
                {
                    client.unregisterAgent(agentId);
                }
                catch (Exception e)
                {
                    logger.warn("Failed to unregister agent", e);
                }
            }

            @Override
            public void publishResult(ResultMessage result)
            {
                client.getProcessorDispatcher().dispatchResult(result);
            }

            @Override
            public void publishReading(ReadingParcelMO reading)
            {
                client.getProcessorDispatcher().dispatchReading(reading);
            }
        };
    }

    protected void connectCluster() throws Exception
    {
        if (! Util.isEmpty(ProxyBaseClient.getProxyUrl(this.configuration.getCluster())))
        {
            logger.info("Connecting to proxy");
            this.client = new ProxyWorkerClient(this.configuration.getCluster(), this::clusterPanic, BergamotVersion.fullVersionString(), this.workerPool, this.engines.keySet());
        }
        else
        {
            logger.info("Connecting to cluster");
            this.client = new HZWorkerClient(this.configuration.getCluster(), this::clusterPanic, DAEMON_NAME, BergamotVersion.fullVersionString(), this.sites, this.workerPool, this.engines.keySet());
        }
    }
    
    protected void clusterPanic(Void v)
    {
        logger.fatal("Connection to cluster lost, forcing shutdown now!");
        this.stop();
    }

    protected void createExecutor() throws Exception
    {
        logger.info("Creating " + this.threadCount + " check executors");
        this.executor = Executors.newFixedThreadPool(this.threadCount, new IBThreadFactory("bergamot-worker-executor", true));
    }

    protected void startEngines() throws Exception
    {
        for (Engine engine : this.getEngines())
        {
            logger.info("Starting check engine: " + engine.getName());
            engine.start(this.createEngineContext(engine));
        }
    }
    
    protected void startConsuming() throws Exception
    {
        logger.info("Starting consuming checks");
        this.client.getWorkerConsumer().start(this.executor, this::executeCheck);
    }

    protected void executeCheck(ExecuteCheck check)
    {
        if (check != null)
        {
            if (logger.isTraceEnabled()) logger.trace("Executing check: " + check);
            CheckExecutionContext context = createExecutionContext(check);
            try
            {
                Engine engine = this.engines.get(check.getEngine());
                if (engine != null && engine.accept(check))
                {
                    engine.execute(check, context);
                }
                else
                {
                    context.publishResult(new ActiveResult().fromCheck(check).error("No engine found to execute check"));
                }
            }
            catch (Exception e)
            {
                context.publishResult(new ActiveResult().fromCheck(check).error("Error executing check: " + e.getMessage()));
            }
        }
    }

    protected CheckExecutionContext createExecutionContext(final ExecuteCheck check)
    {
        return new CheckExecutionContext()
        {
            @Override
            public void publishResult(ResultMessage result)
            {
                result.setProcessor(check.getProcessor());
                client.getProcessorDispatcher().dispatchResult(check.getProcessor(), result);
            }

            @Override
            public void publishReading(ReadingParcelMO reading)
            {
                reading.setProcessor(check.getProcessor());
                client.getProcessorDispatcher().dispatchReading(check.getProcessor(), reading);
            }
        };
    }

    protected void stopConsuming()
    {
        logger.info("Stopping consuming checks");
        this.client.getWorkerConsumer().stop();
    }

    protected void shutdownEngines()
    {
        for (Engine engine : this.getEngines())
        {
            logger.info("Shutting down check engine: " + engine.getName());
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
        try
        {
            logger.info("Bergamot Worker stopping....");
            // Stop consuming
            this.stopConsuming();
            // Shutdown all engines
            this.shutdownEngines();
            // Shutdown executors
            this.shutdownExecutor();
            // Disconnect from the scheduler
            this.disconnectScheduler();
            logger.info("Bergamot Worker stopped.");
        }
        finally
        {
            this.shutdownLatch.countDown();
        }
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

    /**
     * Start this worker daemon
     */
    public static void main(String[] args) throws Exception
    {
        try
        {
            // Load configuration
            WorkerCfg config = loadConfiguration();
            // Configure logging
            configureLogging(config.getLogging());
            Logger logger = Logger.getLogger(BergamotWorker.class);
            logger.info("Bergamot Worker, using configuration:\r\n" + config.toString());
            // Create the worker
            BergamotWorker worker = new BergamotWorker();
            worker.configure(config);
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Triggering shutdown of Bergamot Worker");
                worker.stop();
            }));
            // Start our worker
            worker.start();
            worker.awaitShutdown();
            // Terminate normally
            Thread.sleep(15_000);
            System.exit(0);
        }
        catch (Exception e)
        {
            System.err.println("Failed to start Bergamot Worker!");
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

    private static WorkerCfg loadConfiguration() throws Exception
    {
        WorkerCfg config = null;
        // try the config file?
        File configFile = getConfigurationFile();
        if (configFile.exists())
        {
            System.out.println("Using configuration file " + configFile.getAbsolutePath());
            config = Configuration.read(WorkerCfg.class, new FileInputStream(configFile));
        }
        else
        {
            config = new WorkerCfg();
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

    private static class AvailableEngine
    {
        public final String name;

        public final Supplier<Engine> constructor;

        public final boolean enabledByDefault;

        public AvailableEngine(String name, Supplier<Engine> constructor, boolean enabledByDefault)
        {
            super();
            this.name = name;
            this.constructor = constructor;
            this.enabledByDefault = enabledByDefault;
        }
    }
}

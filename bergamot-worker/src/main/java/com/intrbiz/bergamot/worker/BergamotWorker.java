package com.intrbiz.bergamot.worker;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.broker.AgentEventBroker;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolClientCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.WorkerClientCoordinator;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyClientLookup;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolProducer;
import com.intrbiz.bergamot.cluster.queue.WorkerConsumer;
import com.intrbiz.bergamot.config.LoggingCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
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
        AVAILABLE_ENGINES.add(new AvailableEngine(DummyEngine.NAME,  DummyEngine::new,  true));
        AVAILABLE_ENGINES.add(new AvailableEngine(NagiosEngine.NAME, NagiosEngine::new, true));
        AVAILABLE_ENGINES.add(new AvailableEngine(NRPEEngine.NAME,   NRPEEngine::new,   true));
        AVAILABLE_ENGINES.add(new AvailableEngine(HTTPEngine.NAME,   HTTPEngine::new,   true));
        AVAILABLE_ENGINES.add(new AvailableEngine(JDBCEngine.NAME,   JDBCEngine::new,   true));
        AVAILABLE_ENGINES.add(new AvailableEngine(JMXEngine.NAME,    JMXEngine::new,    true));
        AVAILABLE_ENGINES.add(new AvailableEngine(SSHEngine.NAME,    SSHEngine::new,    true));
        AVAILABLE_ENGINES.add(new AvailableEngine(SFTPEngine.NAME,   SFTPEngine::new,   true));
        AVAILABLE_ENGINES.add(new AvailableEngine(SNMPEngine.NAME,   SNMPEngine::new,   false));
        AVAILABLE_ENGINES.add(new AvailableEngine(AgentEngine.NAME,  AgentEngine::new,   false));
    }
    
    private final UUID id = UUID.randomUUID();
    
    private WorkerCfg configuration;
    
    private Map<String, Engine> engines = new TreeMap<String, Engine>();

    private Set<UUID> sites = new HashSet<>();

    private String workerPool;
    
    private String info;
    
    private String hostName;
    
    private int threadCount;
    
    private HazelcastInstance hazelcast;
    
    private WorkerClientCoordinator workerCoordinator;
    
    private ProcessingPoolClientCoordinator poolCoordinator;
    
    private WorkerConsumer consumer;
    
    private ProcessingPoolProducer producer;
    
    private AgentKeyClientLookup agentKeyLookup;
    
    private AgentEventBroker agentEventBroker;
    
    private Thread[] threads;
    
    private CountDownLatch executorLatch;
    
    private CountDownLatch shutdownLatch;
    
    private AtomicBoolean run;
    
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
    
    public final UUID getId()
    {
        return this.id;
    }

    public final String getWorkerPool()
    {
        return this.workerPool;
    }
    
    public final String getInfo()
    {
        return this.info;
    }

    public final Collection<Engine> getEngines()
    {
        return this.engines.values();
    }
    
    //
    
    @Override
    public final void configure(WorkerCfg cfg) throws Exception
    {
        this.configuration = cfg;
        // TODO: support multiple sites
        if (this.configuration.getSite() != null)
        {
            this.sites.add(this.configuration.getSite());
        }
        this.workerPool = this.configuration.getWorkerPool();
        this.info = this.configuration.getInfo();
        this.hostName = InetAddress.getLocalHost().getHostName();
        this.threadCount = this.configuration.getThreads();
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
        // connect to the scheduler
        this.connectScheduler();
        // prepare our engines
        this.prepareEngines();
        // prepare our executors
        this.createExecutors();
        // start our engines
        this.startEngines();
        // start our executors
        this.startExecutors();
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
        return new EngineContext() {
            @Override
            public Configuration getConfiguration()
            {
                return configuration;
            }

            @Override
            public AgentKeyLookup getAgentKeyLookup()
            {
                return agentKeyLookup;
            }

            @Override
            public AgentEventBroker getAgentEventBroker()
            {
                return agentEventBroker;
            }

            @Override
            public void registerAgent(UUID agentId)
            {
                workerCoordinator.registerAgent(id, agentId);
            }

            @Override
            public void unregisterAgent(UUID agentId)
            {
                workerCoordinator.unregisterAgent(id, agentId);
            }

            @Override
            public void publishResult(ResultMO result)
            {
                producer.publishResult(result);
            }
            
            @Override
            public void publishReading(ReadingParcelMO readingParcelMO)
            {
                producer.publishReading(readingParcelMO);
            }
        };
    }
    
    protected void connectScheduler() throws Exception
    {
        logger.info("Connecting to Bergamot Cluster");
        ClientNetworkConfig netCfg = new ClientNetworkConfig();
        netCfg.setAddresses(this.configuration.getHazelcastClient().getNodes());
        netCfg.setSmartRouting(false);
        ClientConfig cliCfg = new ClientConfig();
        cliCfg.setInstanceName("worker");
        cliCfg.setNetworkConfig(netCfg);
        // Connect to Hazelcast
        this.hazelcast = HazelcastClient.newHazelcastClient(cliCfg);
        // Create our worker lookups
        this.agentKeyLookup = new AgentKeyClientLookup(this.hazelcast);
        this.agentEventBroker = new AgentEventBroker(this.hazelcast);
        // Create our worker coordinator
        this.workerCoordinator = new WorkerClientCoordinator(this.hazelcast);
        this.poolCoordinator = new ProcessingPoolClientCoordinator(this.hazelcast);
        // Register ourselves
        this.producer = this.poolCoordinator.createProcessingPoolProducer();
        this.consumer = this.workerCoordinator.registerWorker(this.id, false, DAEMON_NAME, this.info, this.hostName, this.sites, this.workerPool, this.engines.keySet()); 
    }
    
    protected void createExecutors() throws Exception
    {
        logger.info("Creating " + this.threadCount + " check executors");
        this.threads = new Thread[this.threadCount];
        this.executorLatch = new CountDownLatch(this.threadCount);
        this.run = new AtomicBoolean(false);
        for (int i = 0; i < this.threads.length; i++)
        {
            final int threadNum = i;
            this.threads[i] = new Thread(() -> {
                try
                {
                    logger.debug("Worker executor " + threadNum + " starting.");
                    while (this.run.get())
                    {
                        try
                        {
                            // get a check to execute
                            ExecuteCheck check = this.consumer.poll();
                            if (check != null)
                            {
                                if (logger.isTraceEnabled())
                                    logger.trace("Executing check: " + check);
                                // execute the check
                                CheckExecutionContext context = createExecutionContext(check);
                                Engine engine = this.engines.get(check.getEngine());
                                if (engine != null && engine.accept(check))
                                {
                                    engine.execute(check, context);
                                }
                                else
                                {
                                    context.publishResult(new ActiveResultMO().fromCheck(check).error("No engine found to execute check"));
                                }
                            }
                        }
                        catch (HazelcastException e)
                        {
                            this.clusterPanic(e);
                        }
                        catch (Exception e)
                        {
                            logger.error("Error executing check", e);
                        }
                    }
                    logger.debug("Bergamot Worker executor " + threadNum + " stopped.");
                }
                finally
                {
                    this.executorLatch.countDown();
                }
            }, "Bergamot-Worker-Executor-" + i);
        }
    }
    
    protected void clusterPanic(HazelcastException e)
    {
        // Trigger a shutdown
        if (this.run.compareAndSet(true, false))
        {
            logger.error("Got error communicating with cluster, triggering halt", e);
        }
    }
    
    protected void startExecutors() throws Exception
    {
        logger.info("Starting " + this.threads.length + " check executors");
        this.run.set(true);
        for (int i = 0; i < this.threads.length; i++)
        {
            this.threads[i].start();
        }
    }
    
    protected void startEngines() throws Exception
    {
        for (Engine engine : this.getEngines())
        {
            logger.info("Starting check engine: " + engine.getName());
            engine.start(this.createEngineContext(engine));
        }
    }
    
    protected CheckExecutionContext createExecutionContext(final ExecuteCheck check)
    {
        return new CheckExecutionContext() {
            @Override
            public void publishResult(ResultMO resultMO)
            {
                producer.publishResult(check.getProcessingPool(), resultMO);
            }

            @Override
            public void publishReading(ReadingParcelMO readingParcelMO)
            {
                producer.publishReading(check.getProcessingPool(), readingParcelMO);
            }            
        };
    }
    
    public void run() throws Exception
    {
        // Start this worker
        this.start();
        // Await for all execution threads
        this.awaitExecutors();
        // Stop this worker
        this.stop();
    }
    
    protected void stop()
    {
        try
        {
            logger.info("Bergamot Worker stopping....");
            // Shutdown all engines
            this.shutdownEngines();
            // Disconnect from the scheduler
            this.disconnectScheduler();
            logger.info("Bergamot Worker stopped.");
        }
        finally
        {
            this.shutdownLatch.countDown();
        }
    }
    
    protected void awaitExecutors()
    {
        // Wait for all executors to notify the latch
        while (true)
        {
            try
            {
                this.executorLatch.await();
                break;
            }
            catch (InterruptedException e)
            {
            }
        }
        this.threads = null;
        this.executorLatch = null;
    }
    
    protected void shutdownEngines()
    {
        for (Engine engine : this.getEngines())
        {
            logger.info("Shutting down check engine: " + engine.getName());
            engine.shutdown(this.createEngineContext(engine));
        }
    }
    
    protected void disconnectScheduler()
    {
        logger.info("Disconnecting from Bergamot cluster");
        // Shutdown our consumer
        if (this.consumer != null)
        {
            try
            {
                this.consumer.close();
            }
            catch (HazelcastException e)
            {
            }
        }
        // Shutdown hazelcast
        if (this.hazelcast != null)
            this.hazelcast.shutdown();
        // Reset components
        this.consumer = null;
        this.workerCoordinator = null;
        this.hazelcast = null;
    }
    
    public void shutdown()
    {
        if (this.run.compareAndSet(true, false))
        {
            logger.info("Shutting down Bergamot Worker");
            // Wait for the shutdown to complete
            while (true)
            {
                try
                {
                    this.shutdownLatch.await();
                    break;
                }
                catch (InterruptedException e)
                {
                }
            }
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
            Runtime.getRuntime().addShutdownHook(new Thread(worker::shutdown));
            // Start our worker
            logger.info("Bergamot Worker starting.");
            worker.run();
            // Terminate normally
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Search for the configuration file
     */
    private static File getConfigurationFile()
    {
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("bergamot_config"), System.getenv("BERGAMOT_CONFIG"), DEFAULT_CONFIGURATION_FILE));
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
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.toLevel(Util.coalesceEmpty(config.getLevel(), "info").toUpperCase()));
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

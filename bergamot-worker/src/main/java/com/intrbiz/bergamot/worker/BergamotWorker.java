package com.intrbiz.bergamot.worker;

import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.BergamotConfig;
import com.intrbiz.bergamot.cluster.client.WorkerClient;
import com.intrbiz.bergamot.cluster.client.hz.HZWorkerClient;
import com.intrbiz.bergamot.cluster.client.proxy.ProxyWorkerClient;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.processor.agent.LookupAgentKey;
import com.intrbiz.bergamot.model.message.processor.agent.ProcessorAgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.processor.result.PassiveResult;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;
import com.intrbiz.bergamot.model.message.worker.agent.FoundAgentKey;
import com.intrbiz.bergamot.model.message.worker.agent.WorkerAgentMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckEngine;
import com.intrbiz.bergamot.worker.engine.CheckEngineContext;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.util.IBThreadFactory;
import com.intrbiz.util.IntrbizBootstrap;

public class BergamotWorker
{
    private static final Logger logger = Logger.getLogger(BergamotWorker.class);

    private Set<UUID> sites = new HashSet<>();

    private String workerPool;

    private int threadCount;
    
    private Set<String> enabledEngines = new HashSet<>();
    
    private Set<String> disabledEngines = new HashSet<>();

    private Map<String, CheckEngine> engines = new TreeMap<String, CheckEngine>();

    private WorkerClient client;

    private ExecutorService executor;

    private volatile CountDownLatch shutdownLatch;
    
    private final ConcurrentMap<UUID, Consumer<AgentAuthenticationKey>> agentKeyLookups = new ConcurrentHashMap<>();

    public BergamotWorker()
    {
        super();
    }

    public final Set<UUID> getSites()
    {
        return this.sites;
    }

    public final String getWorkerPool()
    {
        return this.workerPool;
    }

    public final Collection<CheckEngine> getEngines()
    {
        return this.engines.values();
    }

    public final void configure()
    {
        this.sites = BergamotConfig.getSites();
        this.threadCount = BergamotConfig.getThreads(8, 4);
        this.enabledEngines = BergamotConfig.getEnabledEngines();
        this.disabledEngines = BergamotConfig.getDisabledEngines();
        this.workerPool = BergamotConfig.getWorkerPool();
        // register engines
        Set<Class<?>> processed = new HashSet<>();
        for (CheckEngine availableEngine : ServiceLoader.load(CheckEngine.class))
        {
            this.registerEngine(availableEngine, processed);
        }
        for (URLClassLoader pluginLoader : IntrbizBootstrap.getPluginClassLoaders())
        {
            for (CheckEngine availableEngine : ServiceLoader.load(CheckEngine.class, pluginLoader))
            {
                this.registerEngine(availableEngine,processed);
            }    
        }
    }
    
    protected boolean isEngineEnabled(String engineName, boolean enabledByDefault)
    {
        if (this.enabledEngines.contains(engineName))
            return true;
        if (this.disabledEngines.contains(engineName))
            return false;
        return enabledByDefault;
    }
    
    protected void registerEngine(CheckEngine availableEngine, Set<Class<?>> processed)
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
        for (CheckEngine engine : this.getEngines())
        {
            logger.info("Preparing check engine: " + engine.getName());
            engine.prepare(this.createEngineContext(engine));
        }
    }

    protected CheckEngineContext createEngineContext(final CheckEngine e)
    {
        return new CheckEngineContext()
        {
            @Override
            public String getParameter(String name, String defaultValue)
            {
                return BergamotConfig.getConfigurationParameter(name, defaultValue);
            }

            @Override
            public void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback)
            {
                // TODO: caching
                // fire off lookup
                LookupAgentKey lookup = new LookupAgentKey(keyId, client.getId());
                agentKeyLookups.put(lookup.getId(), callback);
                client.getProcessorDispatcher().dispatch(lookup);
            }

            @Override
            public void publishAgentAction(ProcessorAgentMessage event)
            {
                client.getProcessorDispatcher().dispatchAgentMessage(event);
            }

            @Override
            public void registerAgent(UUID agentId, UUID nonce)
            {
                try
                {
                    client.registerAgent(agentId, nonce);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Failed to register agent", e);
                }
            }

            @Override
            public void unregisterAgent(UUID agentId, UUID nonce)
            {
                try
                {
                    client.unregisterAgent(agentId, nonce);
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
            public void publishReading(ReadingParcelMessage reading)
            {
                client.getProcessorDispatcher().dispatchReading(reading);
            }
        };
    }

    protected void connectCluster() throws Exception
    {
        if (! Util.isEmpty(BergamotConfig.getProxyUrl()))
        {
            logger.info("Connecting to proxy");
            this.client = new ProxyWorkerClient(this::clusterPanic, this.getClass().getSimpleName(), BergamotVersion.fullVersionString(), this.workerPool, this.engines.keySet());
        }
        else
        {
            logger.info("Connecting to cluster");
            this.client = new HZWorkerClient(this::clusterPanic, this.getClass().getSimpleName(), BergamotVersion.fullVersionString(), this.sites, this.workerPool, this.engines.keySet());
        }
    }
    
    protected void clusterPanic(Void v)
    {
        logger.fatal("Connection to cluster lost, forcing shutdown now!");
        // Trigger shutdown which will run from the main thread
        this.triggerShutdown();
    }

    protected void createExecutor() throws Exception
    {
        logger.info("Creating " + this.threadCount + " check executors");
        this.executor = Executors.newFixedThreadPool(this.threadCount, new IBThreadFactory("bergamot-worker-executor", true));
    }

    protected void startEngines() throws Exception
    {
        for (CheckEngine engine : this.getEngines())
        {
            logger.info("Starting check engine: " + engine.getName());
            engine.start(this.createEngineContext(engine));
        }
    }
    
    protected void startConsuming() throws Exception
    {
        logger.info("Starting consuming checks");
        this.client.getWorkerConsumer().start(this.executor, this::processMessage);
    }
    
    protected void processMessage(WorkerMessage message)
    {
        if (message instanceof ExecuteCheck)
        {
            this.executeCheck((ExecuteCheck) message);
        }
        else if (message instanceof WorkerAgentMessage)
        {
            this.processAgentMessage((WorkerAgentMessage) message);
        }
    }
    
    protected void processAgentMessage(WorkerAgentMessage message)
    {
        if (message instanceof FoundAgentKey)
        {
            Consumer<AgentAuthenticationKey> callback = this.agentKeyLookups.remove(message.getReplyTo());
            if (callback != null)
            {
                FoundAgentKey found = (FoundAgentKey) message;
                callback.accept(found.getKey() == null ? null : new AgentAuthenticationKey(found.getKey()));
            }
        }
    }

    protected void executeCheck(ExecuteCheck check)
    {
        if (check != null)
        {
            if (logger.isTraceEnabled()) logger.trace("Executing check: " + check);
            CheckExecutionContext context = createExecutionContext(check);
            try
            {
                CheckEngine engine = this.engines.get(check.getEngine());
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
                result.setProcessorId(check.getProcessorId());
                client.getProcessorDispatcher().dispatchResult(result);
            }

            @Override
            public void publishActiveResult(ActiveResult result)
            {
                result.fromCheck(check);
                this.publishResult(result);
            }

            @Override
            public void publishPassiveResult(PassiveResult result)
            {
                this.publishResult(result);
            }

            @Override
            public void publishReading(ReadingParcelMessage reading)
            {
                reading.fromCheck(check);
                reading.setProcessorId(check.getProcessorId());
                client.getProcessorDispatcher().dispatchReading(reading);
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
        for (CheckEngine engine : this.getEngines())
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

    /**
     * Await for shutdown to be triggered
     */
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
            // Configure logging
            BergamotConfig.configureLogging();
            // Create the worker
            BergamotWorker worker = new BergamotWorker();
            worker.configure();
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(worker::triggerShutdown));
            // Run our worker
            worker.run();
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
}

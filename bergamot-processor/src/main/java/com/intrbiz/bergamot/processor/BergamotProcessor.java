package com.intrbiz.bergamot.processor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.AgentProcessorService;
import com.intrbiz.bergamot.cluster.broker.SchedulingTopic;
import com.intrbiz.bergamot.cluster.broker.SiteEventTopic;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZNotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZProcessorConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZWorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProxyDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.WorkerDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.hz.HZNotificationDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.hz.HZProcessorDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.hz.HZProxyDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.hz.HZWorkerDispatcher;
import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.cluster.member.BergamotMember;
import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistar;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.ProxyRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.executor.ProcessorExecutor;
import com.intrbiz.bergamot.leader.BergamotClusterLeader;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;
import com.intrbiz.bergamot.proxy.ProxyProcessorService;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.SchedulingPoolsController;
import com.intrbiz.bergamot.scheduler.WheelScheduler;
import com.intrbiz.lamplighter.reading.DefaultReadingProcessor;
import com.intrbiz.lamplighter.reading.ReadingProcessor;

/**
 * A Bergamot Processing node
 */
public class BergamotProcessor extends BergamotMember
{
    private static final Logger logger = Logger.getLogger(BergamotProcessor.class);
    
    // topics
    
    private final SiteEventTopic siteEventTopic;
    
    private final SiteNotificationTopic notificationTopic;
    
    private final SiteUpdateTopic updateTopic;
    
    private final SchedulingTopic schedulingTopic;
    
    // registries
    
    private final WorkerRegistry workerRegistry;
    
    private final AgentRegistry agentRegistry;
    
    private final NotifierRegistry notifierRegistry;
    
    private final ProcessorRegistry processorRegistry;
    
    private final ProxyRegistry proxyRegistry;
    
    // registars
    
    private final ProcessorRegistar processorRegistar;
    
    // electors
    
    private final LeaderElector leaderElector;
    
    private final SchedulingPoolElector[] poolElectors;
    
    // dispatchers
    
    private final WorkerDispatcher workerDispatcher;
    
    private final NotificationDispatcher notificationDispatcher;
    
    private final ProcessorDispatcher processorDispatcher;
    
    private final ProxyDispatcher proxyDispatcher;
    
    // services
    
    private final Scheduler scheduler;
    
    private final ResultProcessor resultProcessor;
    
    private final ReadingProcessor readingProcessor;
    
    private final AgentProcessorService agentProcessorService;
    
    private final ProxyProcessorService proxyProcessorService;
    
    // consumer
    
    private final ProcessorConsumer processorConsumer;
    
    // schedule controller
    
    private final SchedulingPoolsController schedulingController;
    
    // executor
    
    private final ProcessorExecutor processorExecutor;
    
    // leader
    
    private final BergamotClusterLeader leader;
    
    private final AtomicBoolean started = new AtomicBoolean(false);

    public BergamotProcessor(ClusterCfg config, Consumer<Void> onPanic, String application, String info) throws Exception
    {
        super(config, onPanic, application, info);
        // topics
        this.siteEventTopic = new SiteEventTopic(this.hazelcast);
        this.notificationTopic = new SiteNotificationTopic(this.hazelcast);
        this.updateTopic = new SiteUpdateTopic(this.hazelcast);
        this.schedulingTopic = new SchedulingTopic(this.hazelcast);
        // registries
        this.workerRegistry = new WorkerRegistry(this.zooKeeper.getZooKeeper());
        this.agentRegistry = new AgentRegistry(this.zooKeeper.getZooKeeper());
        this.notifierRegistry = new NotifierRegistry(this.zooKeeper.getZooKeeper());
        this.processorRegistry = new ProcessorRegistry(this.zooKeeper.getZooKeeper());
        this.proxyRegistry = new ProxyRegistry(this.zooKeeper.getZooKeeper());
        // registars
        this.processorRegistar = new ProcessorRegistar(this.zooKeeper.getZooKeeper());
        // elector
        this.leaderElector = new LeaderElector(this.zooKeeper.getZooKeeper(), this.id);
        // TODO: Eventually make scheduling pool count configurable
        this.poolElectors = new SchedulingPoolElector[420];
        for (int i = 0; i < this.poolElectors.length; i++)
        {
            this.poolElectors[i] = new SchedulingPoolElector(this.zooKeeper.getZooKeeper(), i, this.id);
        }
        // dispatchers
        this.workerDispatcher = new HZWorkerDispatcher(this.workerRegistry, this.agentRegistry, this.hazelcast);
        this.notificationDispatcher = new HZNotificationDispatcher(this.notifierRegistry, this.hazelcast);
        this.processorDispatcher = new HZProcessorDispatcher(this.hazelcast, this.processorRegistry.getRouteTable());
        this.proxyDispatcher = new HZProxyDispatcher(this.proxyRegistry, this.hazelcast);
        // services
        this.scheduler = new WheelScheduler(this.id, this.workerDispatcher, this.processorDispatcher);
        this.resultProcessor = new DefaultResultProcessor(this.schedulingTopic, this.notificationDispatcher, this.notificationTopic, this.updateTopic);
        this.readingProcessor = new DefaultReadingProcessor();
        this.agentProcessorService = new AgentProcessorService(this.schedulingTopic, this.notificationDispatcher, this.workerDispatcher, this.poolElectors.length);
        this.proxyProcessorService = new ProxyProcessorService(this.proxyDispatcher);
        // consumer
        this.processorConsumer = new HZProcessorConsumer(this.hazelcast, this.id);
        // scheduling controller
        this.schedulingController = new SchedulingPoolsController(this.poolElectors, this.scheduler, this.schedulingTopic);
        // processor executor
        this.processorExecutor = new ProcessorExecutor(this.resultProcessor, this.readingProcessor, this.agentProcessorService, this.proxyProcessorService, this.processorConsumer);
        // leader
        this.leader = new BergamotClusterLeader(
                this.poolElectors, 
                this.leaderElector, 
                this.processorRegistry,
                (id) -> new HZProcessorConsumer(this.hazelcast, id),
                this.processorDispatcher,
                this.workerRegistry,
                (id) -> new HZWorkerConsumer(this.hazelcast, id),
                this.workerDispatcher,
                this.notifierRegistry,
                (id) -> new HZNotificationConsumer(this.hazelcast, id),
                this.notificationDispatcher
        );
    }
    
    @Override
    protected void panic()
    {
        // Shutdown this processing service
        this.shutdown();
        // Fire the panic
        super.panic();
    }

    /**
     * Launch this processor
     */
    public void start() throws Exception
    {
        if (this.started.compareAndSet(false, true))
        {
            logger.info("Bergamot Processor " + this.id + " starting");
            // Register as a processor
            this.processorRegistar.registerProcessor(new ProcessorRegistration(this.id, System.currentTimeMillis(), this.application, this.info, this.hostName));
            // Wait for other processors to join
            this.waitForProcessors();
            // Start the executor
            this.processorExecutor.start();
            // Start our scheduling pools
            this.schedulingController.start();
            // Start the leader service
            this.leader.start();
            // Elect a leader
            this.leaderElector.elect(this.leader::launch);
        }
    }
    
    protected void waitForProcessors() throws Exception
    {
        if (this.config.getExpectedMembers() > 1)
        {
            int waitFor = (int) Math.ceil(((double) this.config.getExpectedMembers()) / 2d);
            logger.info("Waiting for " + waitFor + " processors to join");
            for (int i = 0; i < 300; i++)
            {
                int count = this.processorRegistry.count();
                if (count >= waitFor)
                    break;
                Thread.sleep(1_000);
            }
        }
    }
    
    public void shutdown()
    {
        if (this.started.compareAndSet(true, false))
        {
            try
            {
                this.leader.stop();
            }
            catch (Exception e)
            {
                logger.error("Failed to halt leader", e);
            }
            try
            {
                this.processorExecutor.stop();
            }
            catch (Exception e)
            {
                logger.error("Failed to stop executor", e);
            }
            try
            {
                this.schedulingController.stop();
            }
            catch (Exception e)
            {
                logger.error("Failed to stop scheduler", e);
            }
        }
    }

    public SiteEventTopic getSiteEventTopic()
    {
        return siteEventTopic;
    }

    public SiteNotificationTopic getNotificationTopic()
    {
        return notificationTopic;
    }

    public SiteUpdateTopic getUpdateTopic()
    {
        return updateTopic;
    }

    public SchedulingTopic getSchedulingTopic()
    {
        return this.schedulingTopic;
    }

    public WorkerRegistry getWorkerRegistry()
    {
        return this.workerRegistry;
    }

    public AgentRegistry getAgentRegistry()
    {
        return this.agentRegistry;
    }

    public NotifierRegistry getNotifierRegistry()
    {
        return this.notifierRegistry;
    }

    public ProcessorRegistry getProcessorRegistry()
    {
        return this.processorRegistry;
    }

    public ProxyRegistry getProxyRegistry()
    {
        return this.proxyRegistry;
    }

    public ProcessorRegistar getProcessorRegistar()
    {
        return this.processorRegistar;
    }

    public LeaderElector getLeaderElector()
    {
        return this.leaderElector;
    }

    public SchedulingPoolElector[] getPoolElectors()
    {
        return this.poolElectors;
    }

    public WorkerDispatcher getWorkerDispatcher()
    {
        return this.workerDispatcher;
    }

    public NotificationDispatcher getNotificationDispatcher()
    {
        return this.notificationDispatcher;
    }
    
    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.processorDispatcher;
    }

    public ProxyDispatcher getProxyDispatcher()
    {
        return this.proxyDispatcher;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public ResultProcessor getResultProcessor()
    {
        return this.resultProcessor;
    }

    public ReadingProcessor getReadingProcessor()
    {
        return this.readingProcessor;
    }

    public AgentProcessorService getAgentProcessorService()
    {
        return this.agentProcessorService;
    }
    
    public ProxyProcessorService getProxyProcessorService()
    {
        return this.proxyProcessorService;
    }

    public SchedulingPoolsController getSchedulingController()
    {
        return this.schedulingController;
    }

    public BergamotClusterLeader getLeader()
    {
        return this.leader;
    }

    public ProcessorConsumer getProcessorConsumer()
    {
        return this.processorConsumer;
    }

    public ProcessorExecutor getProcessorExecutor()
    {
        return this.processorExecutor;
    }
    
    public int getSchedulingPoolCount()
    {
        return this.poolElectors.length;
    }
}

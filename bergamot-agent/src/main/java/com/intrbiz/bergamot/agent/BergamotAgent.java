package com.intrbiz.bergamot.agent;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;

import com.intrbiz.bergamot.agent.client.BergamotAgentClient;
import com.intrbiz.bergamot.agent.handler.AgentInfoHandler;
import com.intrbiz.bergamot.agent.handler.CPUInfoHandler;
import com.intrbiz.bergamot.agent.handler.DefaultHandler;
import com.intrbiz.bergamot.agent.handler.DiskIOHandler;
import com.intrbiz.bergamot.agent.handler.DiskInfoHandler;
import com.intrbiz.bergamot.agent.handler.ExecHandler;
import com.intrbiz.bergamot.agent.handler.MemInfoHandler;
import com.intrbiz.bergamot.agent.handler.MetricsHandler;
import com.intrbiz.bergamot.agent.handler.NetConInfoHandler;
import com.intrbiz.bergamot.agent.handler.NetIOHandler;
import com.intrbiz.bergamot.agent.handler.NetIfInfoHandler;
import com.intrbiz.bergamot.agent.handler.OSInfoHandler;
import com.intrbiz.bergamot.agent.handler.ProcessInfoHandler;
import com.intrbiz.bergamot.agent.handler.ShellHandler;
import com.intrbiz.bergamot.agent.handler.UptimeInfoHandler;
import com.intrbiz.bergamot.agent.handler.WhoInfoHandler;
import com.intrbiz.bergamot.agent.statsd.StatsDProcessor;
import com.intrbiz.bergamot.agent.statsd.StatsDReceiver;
import com.intrbiz.bergamot.agent.util.AgentUtil;
import com.intrbiz.bergamot.model.AuthenticationKey;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.agent.error.AgentError;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;

/**
 */
public class BergamotAgent
{
    public static final String AGENT_VENDOR = "Bergamot Monitoring";
    
    public static final String AGENT_PRODUCT = "Bergamot Agent";
    
    public static final String AGENT_VERSION = "4.0.0";
    
    public static final String USER_AGENT = AGENT_PRODUCT + " (" + AGENT_VENDOR + ") " + AGENT_VERSION;
    
    private static final double BACKOFF_FACTOR = 0.16D;
    
    private static final double BACKOFF_MAX_S = 600;
    
    private static final int BACKOFF_MIN_BLUR = 8000;
    
    private static final Logger logger = Logger.getLogger(BergamotAgent.class);
    
    private URI server;
    
    private UUID agentId;
    
    private String hostName;
    
    private String hostSummary;
    
    private String templateName;
    
    private AuthenticationKey key;
    
    private BergamotAgentClient client;
    
    private ClientHeader headers;
    
    private volatile Channel channel;
    
    private Timer timer;
    
    private AtomicInteger connectionAttempt = new AtomicInteger(0);
    
    private ConcurrentMap<Class<?>, AgentHandler> handlers = new ConcurrentHashMap<Class<?>, AgentHandler>();
    
    private AgentHandler defaultHandler;
    
    private int statsDPort = 0;
    
    private StatsDProcessor statsDProcessor;
    
    private StatsDReceiver statsDReceiver;
    
    private Thread statsDRunner;
    
    public BergamotAgent()
    {
        super();
        // background GC task, we want to deliberately keep memory to a minimum
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                // clean up metrics
                if (BergamotAgent.this.statsDProcessor != null)
                {
                    BergamotAgent.this.statsDProcessor.clearUpStaleMetrics();
                }
                // GC
                System.gc();
                Runtime rt = Runtime.getRuntime();
                logger.debug("Memory, free: " + rt.freeMemory() + " total: " + rt.totalMemory() + " max: " + rt.maxMemory());
            }
        }, 30000L, 30000L);
        // handlers
        this.setDefaultHandler(new DefaultHandler());
        this.registerHandler(new CPUInfoHandler());
        this.registerHandler(new MemInfoHandler());
        this.registerHandler(new DiskInfoHandler());
        this.registerHandler(new OSInfoHandler());
        this.registerHandler(new UptimeInfoHandler());
        this.registerHandler(new NetIfInfoHandler());
        this.registerHandler(new ExecHandler());
        this.registerHandler(new ProcessInfoHandler());
        this.registerHandler(new WhoInfoHandler());
        this.registerHandler(new NetConInfoHandler());
        this.registerHandler(new AgentInfoHandler());
        this.registerHandler(new NetIOHandler());
        this.registerHandler(new DiskIOHandler());
        this.registerHandler(new MetricsHandler());
        this.registerHandler(new ShellHandler());
    }
    
    public StatsDProcessor getStatsDProcessor()
    {
        return this.statsDProcessor;
    }

    public void configure() throws Exception
    {
        // Configure the server URL
        this.configureServerURL();
        // Configure agent details
        this.configureAgentId();
        this.configureAgentHostName();
        this.configureAgentHostSummary();
        this.configureAgentTemplateName();
        // Configure authentication
        this.configureAgentKey();
        // Configure statsD
        this.configureStatsD();
        // create the headers
        this.headers = new ClientHeader().userAgent(USER_AGENT).id(this.agentId).hostName(this.hostName).info(this.hostSummary).templateName(this.templateName);
        // create the client
        logger.info("Bergamot Agent, connecting to " + this.server);
        this.client = new BergamotAgentClient(this.server);
    }
    
    private static String getProperty(String propertyName, boolean required)
    {
        return getProperty(propertyName, null, required);
    }
    
    private static String getProperty(String propertyName, String defaultValue)
    {
        return getProperty(propertyName, defaultValue, false);
    }
    
    private static String getProperty(String propertyName, String defaultValue, boolean required)
    {
        String envVarName = propertyName.toUpperCase().replace('.', '_');
        String value = AgentUtil.coalesce(System.getProperty(propertyName), System.getenv(envVarName), defaultValue);
        if (AgentUtil.isEmpty(value) && required) {
           throw new RuntimeException("Could not find configuration value for property " + propertyName + " (ENV[" + envVarName + "])");
        }
        return value;
    }
    
    private void configureServerURL() throws Exception
    {
        this.server = new URI(getProperty("bergamot.agent.url", true));
    }
    
    private void configureStatsD() throws Exception
    {
        String port = getProperty("statsd.port", false);
        if (! AgentUtil.isEmpty(port))
        {
            this.statsDPort = Integer.parseInt(port);
        }
    }
    
    private void configureAgentId() throws Exception
    {
        // Search for the agent Id
        String agentId = getProperty("bergamot.agent.id", false);
        // TODO: fall back to another agent id source
        // Try to set the agent id
        if (AgentUtil.isEmpty(agentId))
        {
            throw new RuntimeException("Failed to work out Bergamot agent Id, please set 'bergamot.agent.id'!");
        }
        else
        {
            this.agentId = UUID.fromString(agentId);
        }
    }
    
    private void configureAgentHostSummary() throws Exception
    {
        this.hostSummary = getProperty("bergamot.agent.host.summary", false);
    }
    
    private void configureAgentHostName() throws Exception
    {
        // Search for the host name
        String hostName = getProperty("bergamot.agent.host.name", false);
        if (AgentUtil.isEmpty(hostName))
            hostName = this.getHostNameFromLocalHost();
        // Try to set the hostname
        if (AgentUtil.isEmpty(hostName))
        {
            throw new RuntimeException("Failed to work out Bergamot agent host name, please set 'bergamot.agent.host.name'!");
        }
        else
        {
            this.hostName = hostName;
        }
    }
        
    private String getHostNameFromLocalHost()
    {
        try
        {
            return InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException e)
        {
        }
        return null;
    }
    
    private void configureAgentTemplateName() throws Exception
    {
        this.templateName = getProperty("bergamot.agent.template.name", false);
    }
    
    private void configureAgentKey() throws Exception
    {
        this.key = new AuthenticationKey(getProperty("bergamot.agent.key", true));
    }
    
    public void registerHandler(AgentHandler handler)
    {
        for (Class<?> cls : handler.getMessages())
        {
            this.handlers.put(cls, handler);
            handler.init(this);
        }
    }
    
    public void setDefaultHandler(AgentHandler handler)
    {
        this.defaultHandler = handler;
        if (this.defaultHandler != null) this.defaultHandler.init(this);
    }
    
    public AgentHandler getHandler(Class<?> messageType)
    {
        AgentHandler handler = this.handlers.get(messageType);
        return handler == null ? this.defaultHandler : handler;
    }
    
    public void start()
    {
        // init Sigar early on
        try
        {
            logger.info("Bergamot Agent starting on: " + Humidor.getInstance().getSigar().getFQDN());
        }
        catch (SigarException e)
        {
            logger.error("Failed to setup Sigar!", e);
            throw new RuntimeException("Failed to setup Sigar, aborting!", e);
        }
        // setup StatsD
        if (this.statsDPort > 0)
        {
            try
            {
                this.statsDProcessor = new StatsDProcessor();
                this.statsDReceiver = new StatsDReceiver(this.statsDPort, this.statsDProcessor);
                this.statsDRunner = new Thread(this.statsDReceiver);
                this.statsDRunner.start();
            }
            catch (Exception e)
            {
                logger.error("Failed to start StatsD receiver");
            }
        }
        // start the connection process
        this.connect();
    }
    
    protected void handleMessage(Message request, Channel channel)
    {
        if (request instanceof AgentPing)
        {
            logger.debug("Got ping from server");
            channel.writeAndFlush(new AgentPong((AgentPing) request));
        }
        else if (request instanceof AgentPong)
        {
            logger.debug("Got pong from server");
        }
        else if (request instanceof AgentError)
        {
            logger.warn("Got error from server: " + ((AgentError) request).getMessage());
        }
        else if (request != null)
        {
            AgentHandler handler = getHandler(request.getClass());
            if (handler != null)
            {
                Message response = handler.handle(request);
                if (response != null)
                {
                    response.setReplyTo(request.getId());
                    channel.writeAndFlush(response);
                }
            }
            else
            {
                channel.writeAndFlush(new GeneralError(request, "No handler found for request."));
            }
        }
    }

    private void connect()
    {
        // connect the client
        logger.info("Connecting to " + this.server);
        connectionAttempt.incrementAndGet();
        // start the connection attempt
        this.client.connect(this.headers, this.key, this::handleMessage).addListener(connectFuture -> {
            if (connectFuture.isSuccess())
            {
                this.channel = (Channel) connectFuture.get();
                this.channel.closeFuture().addListener(closeFuture -> {
                    logger.info("Lost connection to server");
                    this.scheduleReconnect();
                    this.channel = null;
                });
                logger.info("Sucessfully connected to " + this.server);
            }
            else
            {
                logger.warn("Failed to connect to " + this.server + ": " + connectFuture.cause());
                this.scheduleReconnect();
            }
        });
        
    }
    
    protected void scheduleReconnect()
    {
        long wait = ((long)(Math.min(Math.pow(Math.E, BACKOFF_FACTOR * Math.max(connectionAttempt.get(), 1)), BACKOFF_MAX_S) * 1000L)) + (new SecureRandom().nextInt(BACKOFF_MIN_BLUR));
        logger.info("Scheduling reconnection in " + wait + "ms");
        this.timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                BergamotAgent.this.connect();
            }
        }, wait);
    }
    

    public void stop()
    {
        if (this.channel != null)
        {
            this.channel.close();
        }
        this.client.stop();
        this.statsDReceiver.shutdown();
    }
    
    private static void configureLogging() throws Exception
    {
        String logging = getProperty("bergamot.logging", "console");
        if ("console".equals(logging))
        {
            // configure logging to terminal
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.toLevel(getProperty("bergamot.logging.level", "info").toUpperCase()));
        }
        else
        {
            // configure from file
            PropertyConfigurator.configure(new File(logging).getAbsolutePath());
        }
    }

    public static void main(String[] args) throws Exception
    {
        try
        {
            // setup logging
            configureLogging();
            // start the agent
            BergamotAgent agent = new BergamotAgent();
            agent.configure();
            agent.start();
            // shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run()
                {
                    BergamotAgent.logger.info("Bergamot Agent shutting down");
                    agent.stop();
                }
            });
        }
        catch (Throwable e)
        {
            System.out.println("Failed to start Bergamot Agent");
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}

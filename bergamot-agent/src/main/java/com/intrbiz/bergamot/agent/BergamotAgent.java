package com.intrbiz.bergamot.agent;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;

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
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.error.AgentError;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.GenericFutureListener;

/**
 */
public class BergamotAgent
{
    public static final String AGENT_VENDOR = "Bergamot Monitoring";
    
    public static final String AGENT_PRODUCT = "Bergamot Agent";
    
    public static final String AGENT_VERSION = "4.0.0";
    
    private static final Logger logger = Logger.getLogger(BergamotAgent.class);
    
    private URI server;
    
    private UUID agentId;
    
    private String hostName;
    
    private String templateName;
    
    private AgentAuthenticationKey key;

    private EventLoopGroup eventLoop;
    
    private Timer timer;
    
    private ConcurrentMap<Class<?>, AgentHandler> handlers = new ConcurrentHashMap<Class<?>, AgentHandler>();
    
    private AgentHandler defaultHandler;
    
    private volatile Channel channel;
    
    private AtomicInteger connectionAttempt = new AtomicInteger(0);
    
    private int statsDPort = 0;
    
    private StatsDProcessor statsDProcessor;
    
    private StatsDReceiver statsDReceiver;
    
    private Thread statsDRunner;
    
    public BergamotAgent()
    {
        super();
        this.timer = new Timer();
        // background GC task
        // we want to deliberately 
        // keep memory to a minimum
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
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                BergamotAgent.logger.info("Bergamot Agent shutting down");
                if (BergamotAgent.this.channel != null && BergamotAgent.this.channel.isActive())
                {
                    BergamotAgent.this.channel.close();
                }
            }
        });
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
        this.configureAgentTemplateName();
        // Configure authentication
        this.configureAgentKey();
        // Configure statsD
        this.configureStatsD();
        // Finish configuration
        logger.info("Bergamot Agent, connecting to " + this.server);
        this.eventLoop = new NioEventLoopGroup(1);
    }
    
    private String getProperty(String propertyName, boolean required) throws Exception
    {
        String envVarName = propertyName.toUpperCase().replace('.', '_');
        String value = AgentUtil.coalesce(System.getProperty(propertyName), System.getenv(envVarName));
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
        this.key = new AgentAuthenticationKey(getProperty("bergamot.agent.key", true));
    }
    
    private boolean isSecure()
    {
        return "wss".equalsIgnoreCase(this.server.getScheme()) ||
               "https".equalsIgnoreCase(this.server.getScheme());
    }
    
    private SSLEngine createSSLEngine(String host, int port)
    {
        try
        {
            return SSLContext.getDefault().createSSLEngine();
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.error("Failed to create default SSLContext", e);
        }
        return null;
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

    private void connect()
    {
        final SSLEngine engine = isSecure() ? createSSLEngine(this.server.getHost(), (this.server.getPort() <= 0 ? 443 : this.server.getPort())) : null;
        // configure the client
        Bootstrap b = new Bootstrap();
        b.group(this.eventLoop);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false));
        b.handler(new ChannelInitializer<SocketChannel>()
        {
            @Override
            public void initChannel(SocketChannel ch) throws Exception
            {
                // HTTP handling
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("read-timeout",  new ReadTimeoutHandler(  90 /* seconds */ )); 
                pipeline.addLast("write-timeout", new WriteTimeoutHandler( 90 /* seconds */ ));
                if (engine != null) pipeline.addLast("ssl", new SslHandler(engine));
                pipeline.addLast("codec",         new HttpClientCodec()); 
                pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                pipeline.addLast("handler",       new AgentClientHandler(BergamotAgent.this.timer, BergamotAgent.this.server, BergamotAgent.this.agentId, BergamotAgent.this.hostName, BergamotAgent.this.templateName, BergamotAgent.this.key)
                {
                    @Override
                    protected AgentMessage processAgentMessage(final ChannelHandlerContext ctx, final AgentMessage request)
                    {
                        if (request instanceof AgentPing)
                        {
                            logger.debug("Got ping from server");
                            return new AgentPong((AgentPing) request);
                        }
                        else if (request instanceof AgentPong)
                        {
                            logger.debug("Got pong from server");
                            return null;
                        }
                        else if (request instanceof AgentError)
                        {
                            logger.warn("Got error from server: " + ((AgentError) request).getMessage());
                            return null;
                        }
                        else if (request != null)
                        {
                            AgentHandler handler = getHandler(request.getClass());
                            if (handler != null)
                            {
                                return handler.handle(request);
                            }
                        }
                        return null;
                    }
                });
            }
        });
        // connect the client
        logger.info("Connecting to: " + this.server);
        connectionAttempt.incrementAndGet();
        b.connect(this.server.getHost(), (this.server.getPort() <= 0 ? 443 : this.server.getPort())).addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                final Channel channel = future.channel();
                if (future.isDone() && future.isSuccess())
                {
                    // stash the channel
                    BergamotAgent.this.channel = channel;
                    // reset fail counter
                    connectionAttempt.set(0);
                    // setup close listener
                    channel.closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception
                        {
                            BergamotAgent.this.channel = null;
                            logger.info("Connection closed.");
                            BergamotAgent.this.scheduleReconnect();
                        }
                    });
                }
                else
                {
                    logger.info("Failed to connect to: " + server + "");
                    // schedule reconnect
                    BergamotAgent.this.scheduleReconnect();
                }
            }
        });
    }
    
    protected void scheduleReconnect()
    {
        long wait = Math.min(Math.max(connectionAttempt.get(), 1) * 1000L, 12000L) + (new SecureRandom().nextInt(3000));
        logger.info("Scheduling reconnection in " + wait + "ms");
        this.timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                try
                {
                    BergamotAgent.this.connect();
                }
                catch (Exception e)
                {
                    BergamotAgent.logger.error("Error connecting to server", e);
                    BergamotAgent.this.scheduleReconnect();
                }
            }
        }, wait);
    }

    public void shutdown()
    {
        try
        {
            this.eventLoop.shutdownGracefully().await();
            this.statsDReceiver.shutdown();
        }
        catch (InterruptedException e)
        {
        }
    }
    
    public void terminate()
    {
        try
        {
            this.eventLoop.shutdownGracefully(1, 2, TimeUnit.SECONDS).await();
            this.statsDReceiver.shutdown();
        }
        catch (InterruptedException e)
        {
        }
    }
    
    private static void configureLogging() throws Exception
    {
        String logging = System.getProperty("bergamot.logging", "console");
        if ("console".equals(logging))
        {
            // configure logging to terminal
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.toLevel(System.getProperty("bergamot.logging.level", "info").toUpperCase()));
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
        }
        catch (Throwable e)
        {
            System.out.println("Failed to start Bergamot Agent");
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}

package com.intrbiz.bergamot.agent;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.JAXBException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;

import com.intrbiz.bergamot.agent.config.BergamotAgentCfg;
import com.intrbiz.bergamot.agent.config.Configurable;
import com.intrbiz.bergamot.agent.handler.AgentInfoHandler;
import com.intrbiz.bergamot.agent.handler.CPUInfoHandler;
import com.intrbiz.bergamot.agent.handler.DefaultHandler;
import com.intrbiz.bergamot.agent.handler.DiskInfoHandler;
import com.intrbiz.bergamot.agent.handler.ExecHandler;
import com.intrbiz.bergamot.agent.handler.MemInfoHandler;
import com.intrbiz.bergamot.agent.handler.NetConInfoHandler;
import com.intrbiz.bergamot.agent.handler.NetIfInfoHandler;
import com.intrbiz.bergamot.agent.handler.OSInfoHandler;
import com.intrbiz.bergamot.agent.handler.ProcessInfoHandler;
import com.intrbiz.bergamot.agent.handler.UptimeInfoHandler;
import com.intrbiz.bergamot.agent.handler.WhoInfoHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.error.AgentError;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;

/**
 */
public class BergamotAgent implements Configurable<BergamotAgentCfg>
{
    private Logger logger = Logger.getLogger(BergamotAgent.class);
    
    private URI server;

    private EventLoopGroup eventLoop;
    
    private Timer timer;
    
    private ConcurrentMap<Class<?>, AgentHandler> handlers = new ConcurrentHashMap<Class<?>, AgentHandler>();
    
    private AgentHandler defaultHandler;
    
    private SSLContext sslContext;
    
    private BergamotAgentCfg configuration;
    
    private volatile Channel channel;
    
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
                System.gc();
                Runtime rt = Runtime.getRuntime();
                logger.debug("Memory: " + rt.freeMemory() + " " + rt.totalMemory() + " " + rt.maxMemory());
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
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                BergamotAgent.this.logger.info("Bergamot Agent shutting down");
                if (BergamotAgent.this.channel != null && BergamotAgent.this.channel.isActive())
                {
                    BergamotAgent.this.channel.close();
                }
            }
        });
    }
    
    public BergamotAgentCfg getConfiguration()
    {
        return this.configuration;
    }

    @Override
    public void configure(BergamotAgentCfg cfg) throws Exception
    {
        this.configuration = cfg;
        // configure this agent
        this.server = new URI(cfg.getServer());
        logger.info("Bergamot Agent, connecting to " + this.server + " configured");
        this.eventLoop = new NioEventLoopGroup(1);
        this.sslContext = this.createContext();
    }
    
    private SSLContext createContext()
    {
        try
        {
            String pass = "abc123";
            // create the keystore
            KeyStore sks = KeyStoreUtil.loadClientAuthKeyStore(pass, this.configuration.getKeyTrimmed(), this.configuration.getCertificateTrimmed(), this.configuration.getSiteCaCertificateTrimmed(), this.configuration.getCaCertificateTrimmed());
            KeyStore tks = KeyStoreUtil.loadTrustKeyStore(this.configuration.getCaCertificateTrimmed());
            // the key manager
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(sks, pass.toCharArray());
            // the trust manager
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(tks);
            // the context
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            return context;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to init SSLContext", e);
        }
    }
    
    private SSLEngine createSSLEngine(String host, int port)
    {
        try
        {
            SSLEngine sslEngine = this.sslContext.createSSLEngine(host, port);
            sslEngine.setUseClientMode(true);
            sslEngine.setNeedClientAuth(true);
            SSLParameters params = new SSLParameters();
            // can't do this in JDK 6
            // params.setEndpointIdentificationAlgorithm("HTTPS");
            params.setNeedClientAuth(true);
            sslEngine.setSSLParameters(params);
            return sslEngine;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to init SSLEngine", e);
        }
    }
    
    public URI getServer()
    {
        return this.server;
    }
    
    public void registerHandler(AgentHandler handler)
    {
        for (Class<?> cls : handler.getMessages())
        {
            this.handlers.put(cls, handler);
        }
    }
    
    public void setDefaultHandler(AgentHandler handler)
    {
        this.defaultHandler = handler;
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
        // start the connection process
        this.connect();
    }

    private void connect()
    {
        final SSLEngine engine = createSSLEngine(this.server.getHost(), this.server.getPort());
        // configure the client
        Bootstrap b = new Bootstrap();
        b.group(this.eventLoop);
        b.channel(NioSocketChannel.class);
        b.handler(new ChannelInitializer<SocketChannel>()
        {
            @Override
            public void initChannel(SocketChannel ch) throws Exception
            {
                // HTTP handling
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("read-timeout",  new ReadTimeoutHandler(  90 /* seconds */ )); 
                pipeline.addLast("write-timeout", new WriteTimeoutHandler( 90 /* seconds */ ));
                pipeline.addLast("ssl",           new SslHandler(engine));
                pipeline.addLast("codec",         new HttpClientCodec()); 
                pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                pipeline.addLast("handler",       new AgentClientHandler(BergamotAgent.this.timer, BergamotAgent.this.server)
                {
                    @Override
                    protected AgentMessage processMessage(final ChannelHandlerContext ctx, final AgentMessage request)
                    {
                        if (request instanceof AgentPing)
                        {
                            logger.debug("Got ping from server");
                            return new AgentPong(UUID.randomUUID().toString());
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
        b.connect(this.server.getHost(), (this.server.getPort() <= 0 ? 443 : this.server.getPort())).addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                final Channel channel = future.channel();
                if (future.isDone() && future.isSuccess())
                {
                    // stash the channel
                    BergamotAgent.this.channel = channel;
                    // setup close listener
                    channel.closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception
                        {
                            BergamotAgent.this.channel = null;
                            BergamotAgent.this.scheduleReconnect();
                        }
                    });
                }
                else
                {
                    // schedule reconnect
                    BergamotAgent.this.scheduleReconnect();
                }
            }
        });
    }
    
    protected void scheduleReconnect()
    {
        this.logger.info("Scheduling reconnection shortly");
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
                    BergamotAgent.this.logger.error("Error connecting to server", e);
                    BergamotAgent.this.scheduleReconnect();
                }
            }
        }, 15000L);
    }

    public void shutdown()
    {
        try
        {
            this.eventLoop.shutdownGracefully().await();
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
    
    private static BergamotAgentCfg readConfig() throws JAXBException, FileNotFoundException, IOException
    {
        FileInputStream input = new FileInputStream(new File(System.getProperty("bergamot.agent.config", "/etc/bergamot/agent.xml")));
        try
        {
            return BergamotAgentCfg.read(BergamotAgentCfg.class, input);
        }
        finally
        {
            input.close();
        }
    }
    

    public static void main(String[] args) throws Exception
    {
        // setup loggiing
        configureLogging();
        // load our config
        BergamotAgentCfg config = readConfig();
        // start the agent
        BergamotAgent agent = new BergamotAgent();
        agent.configure(config);
        agent.start();
    }
}

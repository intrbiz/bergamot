package com.intrbiz.bergamot.agent.server;

import java.io.FileReader;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServer.RegisterAgentCallback.SendAgentRegistrationMessage;
import com.intrbiz.bergamot.agent.server.config.BergamotAgentServerCfg;
import com.intrbiz.bergamot.crypto.util.KeyStoreUtil;
import com.intrbiz.bergamot.crypto.util.TLSConstants;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetCon;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIO;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIf;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.check.CheckUptime;
import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationMessage;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationRequest;
import com.intrbiz.bergamot.model.message.agent.util.Parameter;
import com.intrbiz.configuration.Configurable;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

public class BergamotAgentServer implements Runnable, Configurable<BergamotAgentServerCfg>
{
    private Logger logger = Logger.getLogger(BergamotAgentServer.class);

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Thread runner = null;
    
    private ConcurrentMap<UUID, BergamotAgentServerHandler> agents = new ConcurrentHashMap<UUID, BergamotAgentServerHandler>();
    
    private Consumer<BergamotAgentServerHandler> onAgentRegister;
    
    private Consumer<BergamotAgentServerHandler> onAgentUnregister = null;
    
    private Consumer<BergamotAgentServerHandler> onAgentPing;
    
    private RegisterAgentCallback onRequestAgentRegistration;
    
    private SSLContext sslContext;
    
    private BergamotAgentServerCfg configuration;

    public BergamotAgentServer()
    {
        super();
    }
    
    public void configure(BergamotAgentServerCfg cfg)
    {
        this.configuration = cfg;
        // setup the SSL context
        this.sslContext = this.createContext();
    }
    
    public BergamotAgentServerCfg getConfiguration()
    {
        return this.configuration;
    }
    
    private SSLContext createContext()
    {
        try
        {
            String pass = "abc123";
            // create the keystore
            KeyStore sks = KeyStoreUtil.loadClientAuthKeyStore(pass, this.configuration.getKeyTrimmed(), this.configuration.getCertificateTrimmed(), this.configuration.getCaCertificateTrimmed());
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
    
    private SSLEngine createSSLEngine()
    {
        try
        {
            SSLEngine sslEngine = this.sslContext.createSSLEngine();
            // setup ssl params
            SSLParameters params = new SSLParameters();
            params.setNeedClientAuth(true);
            sslEngine.setSSLParameters(params);
            // setup ssl engine
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(true);
            sslEngine.setEnabledProtocols(TLSConstants.PROTOCOLS.SAFE_PROTOCOLS);
            sslEngine.setEnabledCipherSuites(TLSConstants.getCipherNames(TLSConstants.CIPHERS.SAFE_CIPHERS));
            return sslEngine;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to init SSLEngine", e);
        }
    }
    
    public void requestAgentRegistration(UUID templateId, AgentRegistrationRequest request, SendAgentRegistrationMessage callback) throws Exception
    {
        logger.info("Starting registration process of agent under template: " + templateId + " with request:\n" + request);
        if (this.onRequestAgentRegistration != null)
            this.onRequestAgentRegistration.register(templateId, request, callback);
    }
    
    public void registerAgent(BergamotAgentServerHandler agent)
    {
        // register the agent
        this.agents.put(agent.getAgentId(), agent);
        // list registered agents for debugging
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered agents:");
            for (BergamotAgentServerHandler ag : this.agents.values())
            {
                logger.info("Agent: " + ag.getAgentId() + " " + ag.getAgentName());
            }
        }
        // fire the agent register hook
        if (this.onAgentRegister != null) this.onAgentRegister.accept(agent);
    }
    
    public void fireAgentPing(BergamotAgentServerHandler agent)
    {
        // fire the agent ping hook
        if (this.onAgentPing != null) this.onAgentPing.accept(agent);
    }
    
    public void unregisterAgent(BergamotAgentServerHandler agent)
    {
        logger.debug("Agent unregister!");
        this.agents.remove(agent.getAgentId());
        // fire the event
        if (this.onAgentUnregister != null) this.onAgentUnregister.accept(agent);
    }
    
    public BergamotAgentServerHandler getRegisteredAgent(UUID id)
    {
        return this.agents.get(id);
    }
    
    public Collection<BergamotAgentServerHandler> getRegisteredAgents()
    {
        return this.agents.values();
    }
    
    public void setOnAgentRegisterHandler(Consumer<BergamotAgentServerHandler> onAgentRegister)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentRegister = this.onAgentRegister == null ? onAgentRegister : this.onAgentRegister.andThen(onAgentRegister);
        }
    }
    
    public void setOnAgentPingHandler(Consumer<BergamotAgentServerHandler> onAgentPing)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentPing = this.onAgentPing == null ? onAgentPing : this.onAgentPing.andThen(onAgentPing);
        }
    }
    
    public void setOnAgentUnregisterHandler(Consumer<BergamotAgentServerHandler> onAgentUnregister)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentUnregister = this.onAgentUnregister == null ? onAgentUnregister : this.onAgentUnregister.andThen(onAgentUnregister);
        }
    }
    
    public Consumer<BergamotAgentServerHandler> getOnAgentRegister()
    {
        return this.onAgentRegister;
    }
    
    public Consumer<BergamotAgentServerHandler> getOnAgentPing()
    {
        return this.onAgentPing;
    }

    public RegisterAgentCallback getOnRequestAgentRegistration()
    {
        return onRequestAgentRegistration;
    }

    public void setOnRequestAgentRegistration(RegisterAgentCallback onRequestAgentRegistration)
    {
        this.onRequestAgentRegistration = onRequestAgentRegistration;
    }

    public void run()
    {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    SSLEngine engine = createSSLEngine();
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("read-timeout",  new ReadTimeoutHandler(  90 /* seconds */ )); 
                    pipeline.addLast("write-timeout", new WriteTimeoutHandler( 90 /* seconds */ ));
                    pipeline.addLast("ssl",           new SslHandler(engine));
                    pipeline.addLast("codec-http",    new HttpServerCodec());
                    pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                    pipeline.addLast("handler",       new BergamotAgentServerHandler(BergamotAgentServer.this, engine));
                }
            });
            //
            Channel ch = b.bind(this.configuration.getPort()).sync().channel();
            logger.info("Web socket server started at port " + this.configuration.getPort() + '.');
            // await the server to stop
            ch.closeFuture().sync();
            // log
            logger.info("Agent server has shutdown");
        }
        catch (Exception e)
        {
            logger.error("Agent server broke", e);
        }
        finally
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void start()
    {
        if (this.runner == null)
        {
            this.runner = new Thread(this);
            this.runner.start();
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //  the configuration
        BergamotAgentServerCfg cfg = BergamotAgentServerCfg.read(BergamotAgentServerCfg.class, new FileReader("agent-server.xml"));
        System.out.println(cfg);
        // setup the server
        BergamotAgentServer server = new BergamotAgentServer();
        server.configure(cfg);
        // setup handlers
        server.setOnAgentRegisterHandler((agent) -> {
            System.out.println("Agent registered: " + agent.getHello());
            // check the agents CPU usage
            agent.sendMessageToAgent(new CheckCPU(), (response) -> {
                System.out.println("Got CPU usage: " + response);
            });
            // check the agents mem usage
            agent.sendMessageToAgent(new CheckMem(), (response) -> {
                System.out.println("Got Mem usage: " + response);
            });
            // check the agents disk usage
            agent.sendMessageToAgent(new CheckDisk(), (response) -> {
                System.out.println("Got Disk usage: " + response);
            });
            // check the agents os usage
            agent.sendMessageToAgent(new CheckOS(), (response) -> {
                System.out.println("Got OS usage: " + response);
            });
            // check the agents uptime
            agent.sendMessageToAgent(new CheckUptime(), (response) -> {
                System.out.println("Got Uptime: " + response);
            });
            // check the agents network
            agent.sendMessageToAgent(new CheckNetIf(), (response) -> {
                System.out.println("Got Network Info: " + response);
            });
            // exec some shit
            ExecCheck exec = new ExecCheck();
            exec.setEngine("nagios");
            exec.setName("check_mem");
            exec.getParameters().add(new Parameter("command_line", "/usr/lib/nagios/plugins/check_mem -u -C -w 80 -c 90"));
            agent.sendMessageToAgent(exec, (response) -> {
                System.out.println("Got Exec result: " + response);
            });
            // check process
            agent.sendMessageToAgent(new CheckProcess(), (response) -> {
                System.out.println("Got Process Info: " + response);
            });
            // check who
            agent.sendMessageToAgent(new CheckWho(), (response) -> {
                System.out.println("Got Who Info: " + response);
            });
            // check net con
            agent.sendMessageToAgent(new CheckNetCon(), (response) -> {
                System.out.println("Got NetCon Info: " + response);
            });
            //
            try { Thread.sleep(20_000L); } catch (Exception e) {}
            // check net IO
            agent.sendMessageToAgent(new CheckNetIO(), (response) -> {
                System.out.println("Got NetIO Info: " + response);
            });
        });
        // go go go
        server.start();
    }
    
    public static interface RegisterAgentCallback
    {
        public static interface SendAgentRegistrationMessage
        {
            void send(AgentRegistrationMessage response);
        }
        
        void register(UUID templateId, AgentRegistrationRequest request, SendAgentRegistrationMessage callback) throws Exception;
    }
}

package com.intrbiz.bergamot.agent.server;

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

import java.io.File;
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

import com.intrbiz.bergamot.crypto.util.KeyStoreUtil;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIf;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.check.CheckUptime;
import com.intrbiz.bergamot.model.message.agent.hello.AgentHello;

public class AgentServer implements Runnable
{
    private Logger logger = Logger.getLogger(AgentServer.class);

    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Thread runner = null;
    
    private ConcurrentMap<UUID, AgentServerHandler> agents = new ConcurrentHashMap<UUID, AgentServerHandler>();
    
    private Consumer<AgentServerHandler> onAgentRegister;
    
    private SSLContext sslContext;

    public AgentServer(int port)
    {
        super();
        this.port = port;
        this.sslContext = this.createContext();
    }
    
    private SSLContext createContext()
    {
        try
        {
            String pass = "abc123";
            // create the keystore
            KeyStore sks = KeyStoreUtil.loadClientAuthKeyStore(pass, new File("hub.bergamot.local.key"), new File("hub.bergamot.local.crt"), new File("ca.crt"));
            KeyStore tks = KeyStoreUtil.loadTrustKeyStore(new File("ca.crt"));
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
            throw new RuntimeException("Failed to init SSLContext");
        }
    }
    
    private SSLEngine createSSLEngine()
    {
        try
        {
            SSLEngine sslEngine = this.sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(true);
            SSLParameters params = new SSLParameters();
            params.setNeedClientAuth(true);
            sslEngine.setSSLParameters(params);
            return sslEngine;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to init SSLEngine", e);
        }
    }
    
    public void registerAgent(AgentServerHandler agent)
    {
        // register the agent
        AgentHello hello = agent.getHello();
        this.agents.put(hello.getHostId(), agent);
        // list registered agents for debugging
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered agents:");
            for (AgentServerHandler ag : this.agents.values())
            {
                logger.info("  Agent: " + ag.getHello().getHostId() + " " + ag.getHello().getHostName() + " :: " + ag.getHello().getServiceId() + " " + ag.getHello().getServiceName());
            }
        }
        // fire the agent register hook
        if (this.onAgentRegister != null) this.onAgentRegister.accept(agent);
    }
    
    public void unregisterAgent(AgentServerHandler agent)
    {
        AgentHello hello = agent.getHello();
        this.agents.remove(hello.getHostId());
    }
    
    public AgentServerHandler getRegisteredAgent(UUID id)
    {
        return this.agents.get(id);
    }
    
    public Collection<AgentServerHandler> getRegisteredAgents()
    {
        return this.agents.values();
    }
    
    public void setOnAgentRegisterHandler(Consumer<AgentServerHandler> onAgentRegister)
    {
        this.onAgentRegister = onAgentRegister;
    }
    
    public Consumer<AgentServerHandler> getOnAgentRegister()
    {
        return this.onAgentRegister;
    }

    public void run()
    {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try
        {
            SSLEngine engine = createSSLEngine();
            //
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("read-timeout",  new ReadTimeoutHandler(  30 /* seconds */ )); 
                    pipeline.addLast("write-timeout", new WriteTimeoutHandler( 30 /* seconds */ ));
                    pipeline.addLast("ssl",           new SslHandler(engine));
                    pipeline.addLast("codec-http",    new HttpServerCodec());
                    pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                    pipeline.addLast("handler",       new AgentServerHandler(AgentServer.this, engine));
                }
            });
            //
            Channel ch = b.bind(this.port).sync().channel();
            logger.info("Web socket server started at port " + port + '.');
            //
            ch.closeFuture().sync();
        }
        catch (Exception e)
        {
            logger.error("Update server broke", e);
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
        //
        AgentServer s = new AgentServer(8081);
        s.setOnAgentRegisterHandler((agent) -> {
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
        });
        s.start();
    }
}

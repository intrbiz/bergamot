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
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
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

    public AgentServer(int port)
    {
        super();
        this.port = port;
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
                    pipeline.addLast("codec-http",    new HttpServerCodec());
                    pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                    pipeline.addLast("handler",       new AgentServerHandler(AgentServer.this));
                }
            });
            //
            Channel ch = b.bind(port).sync().channel();
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
        });
        s.start();
    }
}

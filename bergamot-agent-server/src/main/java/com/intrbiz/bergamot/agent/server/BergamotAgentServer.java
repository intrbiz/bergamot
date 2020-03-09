package com.intrbiz.bergamot.agent.server;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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

public class BergamotAgentServer implements Runnable
{
    private static final Logger logger = Logger.getLogger(BergamotAgentServer.class);

    private final int port;
    
    private final AgentKeyResolver agentKeyResolver;
    
    private final ConcurrentMap<UUID, BergamotAgentServerHandler> agents = new ConcurrentHashMap<UUID, BergamotAgentServerHandler>();
    
    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Thread runner = null;
    
    private volatile Consumer<BergamotAgentServerHandler> onAgentPing;
    
    private volatile Consumer<BergamotAgentServerHandler> onAgentConnect;
    
    private volatile Consumer<BergamotAgentServerHandler> onAgentDisconnect;

    public BergamotAgentServer(int port, AgentKeyResolver agentKeyResolver)
    {
        super();
        this.port = port;
        this.agentKeyResolver = agentKeyResolver;
    }
    
    public AgentKeyResolver getAgentKeyResolver()
    {
        return this.agentKeyResolver;
    }

    private boolean isSecure()
    {
        return false;
    }
    
    private SSLEngine createSSLEngine()
    {
        // TODO !!!
        return null;
    }
    
    public BergamotAgentServerHandler getAgent(UUID id)
    {
        return this.agents.get(id);
    }
    
    public Collection<BergamotAgentServerHandler> getAgents()
    {
        return this.agents.values();
    }
    
    // Event handlers
    
    public void setOnAgentPingHandler(Consumer<BergamotAgentServerHandler> onAgentPing)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentPing = this.onAgentPing == null ? onAgentPing : this.onAgentPing.andThen(onAgentPing);
        }
    }
    
    public Consumer<BergamotAgentServerHandler> getOnAgentPing()
    {
        return this.onAgentPing;
    }
    
    public void setOnAgentConnectHandler(Consumer<BergamotAgentServerHandler> onAgentConnect)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentConnect = this.onAgentConnect == null ? onAgentConnect : this.onAgentConnect.andThen(onAgentConnect);
        }
    }
    
    public Consumer<BergamotAgentServerHandler> getOnAgentConnect()
    {
        return this.onAgentConnect;
    }
    
    public void setOnAgentDisconnectHandler(Consumer<BergamotAgentServerHandler> onAgentDisconnect)
    {
        synchronized (this)
        {
            // set the handler or chain them
            this.onAgentDisconnect = this.onAgentDisconnect == null ? onAgentDisconnect : this.onAgentDisconnect.andThen(onAgentDisconnect);
        }
    }
    
    public Consumer<BergamotAgentServerHandler> getOnAgentDisconnect()
    {
        return this.onAgentDisconnect;
    }
    
    // Event Handlers
    
    public void fireAgentPing(BergamotAgentServerHandler agent)
    {
        // fire the agent ping hook
        if (this.onAgentPing != null) this.onAgentPing.accept(agent);
    }
    
    public void fireAgentConnect(BergamotAgentServerHandler agent)
    {
        this.agents.put(agent.getAgentId(), agent);
        // fire the agent ping hook
        if (this.onAgentConnect != null) this.onAgentConnect.accept(agent);
    }
    
    public void fireAgentDisconnect(BergamotAgentServerHandler agent)
    {
        this.agents.remove(agent.getAgentId());
        // fire the agent ping hook
        if (this.onAgentDisconnect != null) this.onAgentDisconnect.accept(agent);
    }

    // Lifecycle handlers
    
    public void run()
    {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
            b.childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch) throws Exception
                {
                    SSLEngine engine = isSecure() ? createSSLEngine() : null;
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("read-timeout",  new ReadTimeoutHandler(  90 /* seconds */ )); 
                    pipeline.addLast("write-timeout", new WriteTimeoutHandler( 90 /* seconds */ ));
                    if (engine != null) pipeline.addLast("ssl", new SslHandler(engine));
                    pipeline.addLast("codec-http",    new HttpServerCodec());
                    pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                    pipeline.addLast("handler",       new BergamotAgentServerHandler(BergamotAgentServer.this));
                }
            });
            //
            Channel ch = b.bind(this.port).sync().channel();
            logger.info("Web socket server started at port " + this.port + '.');
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
}

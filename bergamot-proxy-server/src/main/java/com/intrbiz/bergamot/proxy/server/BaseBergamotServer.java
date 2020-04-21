package com.intrbiz.bergamot.proxy.server;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.proxy.KeyResolver;
import com.intrbiz.bergamot.proxy.codec.BergamotMessageDecoder;
import com.intrbiz.bergamot.proxy.codec.BergamotMessageEncoder;
import com.intrbiz.bergamot.proxy.server.handler.HealthHandler;
import com.intrbiz.bergamot.proxy.server.handler.MessageHandler;
import com.intrbiz.bergamot.proxy.server.handler.WebSocketHandler;

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
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.Future;

public abstract class BaseBergamotServer
{
    private static final Logger logger = Logger.getLogger(BaseBergamotServer.class);
    
    private final int port;
    
    private final KeyResolver keyResolver;
    
    private final MessageProcessor.Factory processorFactory;
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;
    
    private Channel serverChannel;

    public BaseBergamotServer(int port, KeyResolver keyResolver, MessageProcessor.Factory processorFactory)
    {
        super();
        this.port = port;
        this.keyResolver = Objects.requireNonNull(keyResolver);
        this.processorFactory = Objects.requireNonNull(processorFactory);
    }
    
    public KeyResolver getKeyResolver()
    {
        return this.keyResolver;
    }
    
    protected abstract String getServerName();
    
    protected abstract String getWebSocketPath();
    
    public void start()
    {
        if (this.started.compareAndSet(false, true))
        {
            this.bossGroup = new NioEventLoopGroup(1);
            this.workerGroup = new NioEventLoopGroup();
            try
            {
                ServerBootstrap b = new ServerBootstrap();
                b.group(this.bossGroup, this.workerGroup);
                b.channel(NioServerSocketChannel.class);
                b.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
                b.childHandler(new ChannelInitializer<SocketChannel>()
                {
                    private final HealthHandler health = new HealthHandler(getServerName());
                    
                    private final BergamotMessageDecoder decoder = new BergamotMessageDecoder();
                    
                    private final BergamotMessageEncoder encoder = new BergamotMessageEncoder();
                    
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception
                    {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("read-timeout",  new ReadTimeoutHandler(90 /* seconds */)); 
                        pipeline.addLast("write-timeout", new WriteTimeoutHandler(90 /* seconds */));
                        pipeline.addLast("codec-http",    new HttpServerCodec());
                        pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                        pipeline.addLast("health",        this.health);
                        pipeline.addLast("websocket",     new WebSocketHandler(getWebSocketPath(), getServerName(), getKeyResolver()));
                        pipeline.addLast("decoder",       this.decoder);
                        pipeline.addLast("encoder",       this.encoder);
                        pipeline.addLast("handler",       new MessageHandler(BaseBergamotServer.this.processorFactory));
                    }
                });
                //
                this.serverChannel = b.bind(this.port).sync().channel();
                logger.info(this.getServerName() + " server started at port " + this.port + '.');
            }
            catch (Exception e)
            {
                logger.error(this.getServerName() + " failed to start!", e);
            }
        }
    }
    
    public void stop()
    {
        if (this.started.compareAndSet(true, false))
        {
            // Shutdown the server socket
            if (this.serverChannel != null)
            {
                try
                {
                    this.serverChannel.close().await();
                }
                catch (InterruptedException e)
                {
                }
            }
            // Shutdown the executors
            Future<?> bossFuture   = this.bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            Future<?> workerFuture = this.workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            try
            {
                workerFuture.await();
            }
            catch (InterruptedException e)
            {
            }
            try
            {
                bossFuture.await();
            }
            catch (InterruptedException e)
            {
            }
            this.serverChannel = null;
            this.bossGroup = null;
            this.workerGroup = null;
        }
    }
}

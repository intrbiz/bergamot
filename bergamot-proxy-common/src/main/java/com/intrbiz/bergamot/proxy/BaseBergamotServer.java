package com.intrbiz.bergamot.proxy;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.proxy.auth.KeyResolver;
import com.intrbiz.bergamot.proxy.codec.BergamotMessageDecoder;
import com.intrbiz.bergamot.proxy.codec.BergamotMessageEncoder;
import com.intrbiz.bergamot.proxy.handler.server.HealthHandler;
import com.intrbiz.bergamot.proxy.handler.server.MessageHandler;
import com.intrbiz.bergamot.proxy.handler.server.WebSocketHandler;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.processor.MessageProcessor;

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
import io.netty.util.AsciiString;
import io.netty.util.concurrent.Future;

public abstract class BaseBergamotServer
{
    private static final Logger logger = Logger.getLogger(BaseBergamotServer.class);
    
    private final int port;
    
    private final AsciiString serverName;
    
    private final String websocketPath;
    
    private final BergamotTranscoder transcoder;
    
    private final KeyResolver keyResolver;
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;
    
    private Channel serverChannel;

    public BaseBergamotServer(int port, String serverName, String websocketPath, BergamotTranscoder transcoder, KeyResolver keyResolver)
    {
        super();
        this.port = port;
        this.serverName = AsciiString.cached(Objects.requireNonNull(serverName));
        this.websocketPath = Objects.requireNonNull(websocketPath);
        this.transcoder = Objects.requireNonNull(transcoder);
        this.keyResolver = Objects.requireNonNull(keyResolver);
    }
    
    public int getPort()
    {
        return this.port;
    }

    public AsciiString getServerName()
    {
        return this.serverName;
    }

    public String getWebsocketPath()
    {
        return this.websocketPath;
    }

    public KeyResolver getKeyResolver()
    {
        return this.keyResolver;
    }
    
    public Executor getServerExecutor()
    {
        return this.workerGroup;
    }
    
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
                    private final HealthHandler health = new HealthHandler(serverName);
                    
                    private final BergamotMessageDecoder decoder = new BergamotMessageDecoder(transcoder);
                    
                    private final BergamotMessageEncoder encoder = new BergamotMessageEncoder(transcoder);
                    
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception
                    {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("read-timeout",  new ReadTimeoutHandler(45 /* seconds */)); 
                        pipeline.addLast("write-timeout", new WriteTimeoutHandler(45 /* seconds */));
                        pipeline.addLast("codec-http",    new HttpServerCodec());
                        pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                        pipeline.addLast("health",        this.health);
                        pipeline.addLast("websocket",     new WebSocketHandler(websocketPath, serverName, getKeyResolver()));
                        pipeline.addLast("decoder",       this.decoder);
                        pipeline.addLast("encoder",       this.encoder);
                        pipeline.addLast("handler",       new MessageHandler(BaseBergamotServer.this::create, BaseBergamotServer.this::close));
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
    
    protected abstract MessageProcessor create(ClientHeader client, Channel channel);
    
    protected abstract void close(MessageProcessor processor);
    
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

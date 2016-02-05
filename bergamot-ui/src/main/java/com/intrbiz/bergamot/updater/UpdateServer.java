package com.intrbiz.bergamot.updater;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.updater.handler.DefaultHandler;
import com.intrbiz.bergamot.updater.handler.ExecuteAdhocChecksHandler;
import com.intrbiz.bergamot.updater.handler.PingHandler;
import com.intrbiz.bergamot.updater.handler.RegisterForAdhocResultsHandler;
import com.intrbiz.bergamot.updater.handler.RegisterForNotificationsHandler;
import com.intrbiz.bergamot.updater.handler.RegisterForUpdatesHandler;
import com.intrbiz.bergamot.updater.handler.RequestHandler;

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

public class UpdateServer implements Runnable
{
    private Logger logger = Logger.getLogger(UpdateServer.class);

    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Thread runner = null;
    
    private ConcurrentMap<Class<?>, RequestHandler<?>> handlers = new ConcurrentHashMap<Class<?>, RequestHandler<?>>();
    
    private RequestHandler<?> defaultHandler;

    public UpdateServer(int port)
    {
        super();
        this.port = port;
        // register default handlers
        this.registerDefaultHandler(new DefaultHandler());
        this.registerHandler(new PingHandler());
        this.registerHandler(new RegisterForUpdatesHandler());
        this.registerHandler(new RegisterForNotificationsHandler());
        this.registerHandler(new RegisterForAdhocResultsHandler());
        this.registerHandler(new ExecuteAdhocChecksHandler());
    }
    
    public void registerHandler(RequestHandler<?> handler)
    {
       for (Class<?> type : handler.getRequestTypes())
       {
           this.handlers.put(type, handler);
       }
    }
    
    public void registerDefaultHandler(RequestHandler<?> handler)
    {
        this.defaultHandler = handler;
    }
    
    public RequestHandler<?> getHandler(Class<?> type)
    {
        RequestHandler<?> handler = this.handlers.get(type);
        return handler == null ? this.defaultHandler : handler;
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
                    pipeline.addLast("codec-http", new HttpServerCodec());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                    pipeline.addLast("handler",    new WebSocketServerHandler(UpdateServer.this));
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
}

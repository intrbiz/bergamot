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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class AgentServer implements Runnable
{
    private Logger logger = Logger.getLogger(AgentServer.class);

    private int port;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Thread runner = null;

    public AgentServer(int port)
    {
        super();
        this.port = port;
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
                    pipeline.addLast("handler", new WSServerHandler());
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
        s.start();
    }
}

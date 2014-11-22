package com.intrbiz.bergamot.agent;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.URI;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.util.IBThreadFactory;

/**
 */
public class BergamotAgent
{
    private URI server;

    private EventLoopGroup eventLoop;
    
    private Channel channel;

    public BergamotAgent(URI server)
    {
        this.server = server;
        this.eventLoop = new NioEventLoopGroup(1, new IBThreadFactory("bergamot-agent", false));
    }
    
    public Channel getChannel()
    {
        return this.channel;
    }

    public void connect() throws Exception
    {
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
                ch.pipeline().addLast("codec",      new HttpClientCodec()); 
                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                ch.pipeline().addLast("handler",    new WSClientHandler(BergamotAgent.this.server));
            }
        });
        // connect the client
        this.channel = b.connect(this.server.getHost(), this.server.getPort()).sync().channel();
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

    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        BergamotAgent agent = new BergamotAgent(new URI("ws://127.0.0.1:8081/websocket"));
        agent.connect();
    }
}

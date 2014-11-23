package com.intrbiz.bergamot.agent;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.util.IBThreadFactory;

/**
 */
public class BergamotAgent
{
    private Logger logger = Logger.getLogger(BergamotAgent.class);
    
    private URI server;

    private EventLoopGroup eventLoop;
    
    private Timer timer;

    public BergamotAgent(URI server)
    {
        this.server = server;
        this.eventLoop = new NioEventLoopGroup(1, new IBThreadFactory("bergamot-agent", false));
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
        }, 30_000L, 30_000L);
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
        b.connect(this.server.getHost(), this.server.getPort()).addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                final Channel channel = future.channel();
                if (future.isDone() && future.isSuccess())
                {
                    // setup close listener
                    channel.closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception
                        {
                            BergamotAgent.this.scheduleReconnect();
                        }
                    });
                    // setup period ping
                    BergamotAgent.this.timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run()
                        {
                            if (channel.isActive())
                            {
                                BergamotAgent.this.logger.debug("Sending ping to server");
                                channel.writeAndFlush(new TextWebSocketFrame("{\"type\":\"bergamot.api.util.ping\",\"request_id\":\"testing_12345\"}"));
                            }
                            else
                            {
                                this.cancel();
                            }
                        }
                    }, 15_000L, 15_000L);
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
        }, 15_000L);
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

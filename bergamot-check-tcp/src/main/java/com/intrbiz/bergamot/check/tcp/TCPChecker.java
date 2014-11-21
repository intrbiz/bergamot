package com.intrbiz.bergamot.check.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.intrbiz.util.IBThreadFactory;

/**
 * A non-blocking, asynchronous HTTP checker
 */
public class TCPChecker
{    
    private EventLoopGroup eventLoop;

    private int defaultRequestTimeoutSeconds;

    private int defaultConnectTimeoutSeconds;

    public TCPChecker(int threads, int defaultConnectTimeoutSeconds, int defaultRequestTimeoutSeconds)
    {
        this.defaultRequestTimeoutSeconds = defaultRequestTimeoutSeconds;
        this.defaultConnectTimeoutSeconds = defaultConnectTimeoutSeconds;
        // setup the Netty event loop
        this.eventLoop = new NioEventLoopGroup(threads, new IBThreadFactory("bergamot-http-checker", false));
    }

    public TCPChecker()
    {
        this((Runtime.getRuntime().availableProcessors() * 2) + 4, 5, 60);
    }
    
    public TCPChecker(int threads)
    {
        this(threads, 5, 60);
    }

    public int getDefaultRequestTimeoutSeconds()
    {
        return defaultRequestTimeoutSeconds;
    }

    public int getDefaultConnectTimeoutSeconds()
    {
        return defaultConnectTimeoutSeconds;
    }
    
    protected void submit(
            final String host, 
            final int port, 
            final int connectTimeout, 
            final int requestTimeout, 
            final Consumer<TCPCheckResponse> responseHandler, 
            final Consumer<Throwable> errorHandler
    )
    {
        // configure the client
        Bootstrap b = new Bootstrap();
        b.group(this.eventLoop);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(connectTimeout < 0 ? this.defaultConnectTimeoutSeconds : connectTimeout));
        b.handler(new ChannelInitializer<SocketChannel>()
        {
            @Override
            public void initChannel(SocketChannel ch) throws Exception
            {
                // Timeouts
                ch.pipeline().addLast(
                        new ReadTimeoutHandler(  requestTimeout < 0 ? TCPChecker.this.defaultRequestTimeoutSeconds : requestTimeout /* seconds */ ), 
                        new WriteTimeoutHandler( requestTimeout < 0 ? TCPChecker.this.defaultRequestTimeoutSeconds : requestTimeout /* seconds */ )
                );
                // HTTP handling
                ch.pipeline().addLast(
                        new HttpClientCodec(),
                        new HttpContentDecompressor(),
                        new HttpObjectAggregator(1 * 1024 * 1024 /* 1 MiB */),
                        new TCPClientHandler(responseHandler, errorHandler)
                );
            }
        });
        // connect the client
        b.connect(host, port).addListener(new GenericFutureListener<ChannelFuture>()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if (future.isDone() && (!future.isSuccess()))
                {
                    errorHandler.accept(future.cause());
                }
            }
        });
    }
    
    public TCPCheckBuilder check()
    {
        return new TCPCheckBuilder() {
            @Override
            protected void submit(
                    String address, 
                    int port, 
                    int connectTimeout, 
                    int requestTimeout,  
                    Consumer<TCPCheckResponse> responseHandler, 
                    Consumer<Throwable> errorHandler
            )
            {
                TCPChecker.this.submit(
                        address, 
                        port, 
                        connectTimeout, 
                        requestTimeout, 
                        responseHandler, 
                        errorHandler
                );
            }
        };
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
}

package com.intrbiz.bergamot.proxy;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.AuthenticationKey;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.proxy.codec.BergamotMessageDecoder;
import com.intrbiz.bergamot.proxy.codec.BergamotMessageEncoder;
import com.intrbiz.bergamot.proxy.handler.client.MessageHandler;
import com.intrbiz.bergamot.proxy.handler.client.WebSocketHandler;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public class BaseBergamotClient
{
    private static Logger logger = Logger.getLogger(BaseBergamotClient.class);
    
    private final URI server;
    
    private final BergamotTranscoder transcoder;

    private SSLContext sslContext;
    
    private EventLoopGroup eventLoop;
    
    private Timer timer;
 
    public BaseBergamotClient(URI server, BergamotTranscoder transcoder)
    {
        super();
        this.server = Objects.requireNonNull(server);
        this.transcoder = Objects.requireNonNull(transcoder);
        this.eventLoop = new NioEventLoopGroup(1);
        this.timer = new Timer("bergamot-proxy-client-timer", true);
        // Create our SSL context
        try
        {
            this.sslContext = SSLContext.getDefault();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Failed to create default SSLContext", e);
        }
    }
    
    public URI getServer()
    {
        return this.server;
    }
    
    public boolean isSecure()
    {
        return "wss".equalsIgnoreCase(this.server.getScheme()) ||
               "https".equalsIgnoreCase(this.server.getScheme());
    }
    
    public String getHost()
    {
        return this.server.getHost();
    }
    
    public int getPort()
    {
        return this.server.getPort() <= 0 ? (this.isSecure() ? 443 : 80) : this.server.getPort();
    }
    
    private SSLEngine createSSLEngine(String host, int port)
    {
        SSLEngine sslEngine = this.sslContext.createSSLEngine(host, port);
        sslEngine.setUseClientMode(true);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }
    
    public Future<Channel> connect(final ClientHeader client, final AuthenticationKey key, final BiConsumer<Message, Channel> messageHandler)
    {
        if (logger.isTraceEnabled()) logger.trace("Connecting to: " + this.server);
        Promise<Channel> connectPromise = this.eventLoop.next().newPromise();
        new Bootstrap().group(this.eventLoop)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false))
        .handler(new ChannelInitializer<SocketChannel>()
        {
            private final BergamotMessageDecoder decoder = new BergamotMessageDecoder(transcoder);
            
            private final BergamotMessageEncoder encoder = new BergamotMessageEncoder(transcoder);
            
            @Override
            public void initChannel(SocketChannel ch) throws Exception
            {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("read-timeout",  new ReadTimeoutHandler(  45 /* seconds */ )); 
                pipeline.addLast("write-timeout", new WriteTimeoutHandler( 45 /* seconds */ ));
                if (isSecure()) pipeline.addLast("ssl", new SslHandler(createSSLEngine(getHost(), getPort())));
                pipeline.addLast("codec",         new HttpClientCodec()); 
                pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                pipeline.addLast("websocket",     new WebSocketHandler(timer, server, client, key, connectPromise));
                pipeline.addLast("decoder",       this.decoder);
                pipeline.addLast("encoder",       this.encoder);
                pipeline.addLast("handler",       new MessageHandler(messageHandler));
            }
        })
        .connect(this.getHost(), this.getPort()).addListener((future) -> {
            if (future.isDone() && (! future.isSuccess()))
            {
                connectPromise.setFailure(future.cause());
            }
        });
        return connectPromise;
    }
    
    public void stop()
    {
        try
        {
            this.eventLoop.shutdownGracefully(0, 5, TimeUnit.SECONDS).await();
        }
        catch (InterruptedException e)
        {
        }
        this.timer.cancel();
    }
}

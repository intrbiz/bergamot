package com.intrbiz.bergamot.proxy.client;

import java.net.URI;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.proxy.codec.BergamotMessageDecoder;
import com.intrbiz.bergamot.proxy.codec.BergamotMessageEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
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

public class BergamotProxyClient
{
    private static Logger logger = Logger.getLogger(BergamotProxyClient.class);
    
    private final URI server;

    private SSLContext sslContext;
    
    private EventLoopGroup eventLoop;
 
    public BergamotProxyClient(URI server)
    {
        super();
        this.server = server;
        this.eventLoop = new NioEventLoopGroup(1);
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
    
    private boolean isSecure()
    {
        return "wss".equalsIgnoreCase(this.server.getScheme()) ||
               "https".equalsIgnoreCase(this.server.getScheme());
    }
    
    private String getHost()
    {
        return this.server.getHost();
    }
    
    private int getPort()
    {
        return (this.server.getPort() <= 0 ? (this.isSecure() ? 80 : 443) : this.server.getPort());
    }
    
    private SSLEngine createSSLEngine(String host, int port)
    {
        SSLEngine sslEngine = this.sslContext.createSSLEngine(host, port);
        sslEngine.setUseClientMode(true);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }
    
    public ChannelFuture connect()
    {
        final SSLEngine engine = isSecure() ? createSSLEngine(this.getHost(), this.getPort()) : null;
        // configure the client
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoop);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false));
        bootstrap.handler(new ChannelInitializer<SocketChannel>()
        {
            private final BergamotMessageDecoder decoder = new BergamotMessageDecoder();
            
            private final BergamotMessageEncoder encoder = new BergamotMessageEncoder();
            
            @Override
            public void initChannel(SocketChannel ch) throws Exception
            {
                // HTTP handling
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("read-timeout",  new ReadTimeoutHandler(  45 /* seconds */ )); 
                pipeline.addLast("write-timeout", new WriteTimeoutHandler( 45 /* seconds */ ));
                if (engine != null) pipeline.addLast("ssl", new SslHandler(engine));
                pipeline.addLast("codec",         new HttpClientCodec()); 
                pipeline.addLast("aggregator",    new HttpObjectAggregator(65536));
                pipeline.addLast("decoder",       this.decoder);
                pipeline.addLast("encoder",       this.encoder);
            }
        });
        // connect the client
        logger.info("Connecting to: " + this.server);
        return bootstrap.connect(this.getHost(), this.getPort());
    }
}

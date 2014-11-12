package com.intrbiz.bergamot.worker.check.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.util.IBThreadFactory;

/**
 * A non-blocking, asynchronous HTTP checker
 */
public class HTTPChecker
{   
    private final SSLContext defaultContext;
    
    private final SSLContext badCertContext;
    
    private EventLoopGroup eventLoop;

    private int defaultRequestTimeoutSeconds;

    private int defaultConnectTimeoutSeconds;

    public HTTPChecker(int threads, int defaultConnectTimeoutSeconds, int defaultRequestTimeoutSeconds)
    {
        this.defaultRequestTimeoutSeconds = defaultRequestTimeoutSeconds;
        this.defaultConnectTimeoutSeconds = defaultConnectTimeoutSeconds;
        // setup ssl contexts
        this.defaultContext = this.createContext(false);
        this.badCertContext = this.createContext(true);
        // setup the Netty event loop
        this.eventLoop = new NioEventLoopGroup(threads, new IBThreadFactory("bergamot-http-checker", false));
    }

    public HTTPChecker()
    {
        this((Runtime.getRuntime().availableProcessors() * 2) + 4, 5, 60);
    }
    
    public HTTPChecker(int threads)
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
    
    private SSLContext createContext(boolean permitInvalidCerts)
    {
        try
        {
            /* 
             * Setup a custom SSLContext which will not validate certs
             */
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new BergamotTrustManager(permitInvalidCerts) }, new SecureRandom());
            return context;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to init SSLContext");
        }
    }
    
    /**
     * Create an SSL engine
     */
    private SSLEngine createSSLEngine(boolean permitInvalidCerts)
    {
        try
        {
            SSLContext context = permitInvalidCerts ? this.badCertContext : this.defaultContext;
            /*
             * Setup the SSL engine
             * 
             * customise the protocols and cipher suites we will use
             * these represent a safe best practice baseline, this prohibits:
             *  * SSLv3
             *  * RC4
             *  * 3DES
             *  * MD5
             */
            SSLEngine sslEngine = context.createSSLEngine();
            sslEngine.setEnabledProtocols(TLSConstants.PROTOCOLS.SAFE_PROTOCOLS);
            sslEngine.setEnabledCipherSuites(TLSConstants.CIPHERS.SAFE_CIPHERS);
            sslEngine.setUseClientMode(true);
            return sslEngine;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to init SSLEngine", e);
        }
    }
    
    public void submit(final String host, final int port, final int connectTimeout, final int requestTimeout, final boolean ssl, final boolean permitInvalidCerts, final FullHttpRequest request, final Consumer<HTTPCheckResponse> responseHandler, final Consumer<Throwable> errorHandler)
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
                        new ReadTimeoutHandler(  requestTimeout < 0 ? HTTPChecker.this.defaultRequestTimeoutSeconds : requestTimeout /* seconds */ ), 
                        new WriteTimeoutHandler( requestTimeout < 0 ? HTTPChecker.this.defaultRequestTimeoutSeconds : requestTimeout /* seconds */ )
                );
                // SSL support
                if (ssl) ch.pipeline().addLast(new SslHandler(createSSLEngine(permitInvalidCerts)));
                // HTTP handling
                ch.pipeline().addLast(
                        new HttpClientCodec(),
                        new HttpContentDecompressor(),
                        new HttpObjectAggregator(1 * 1024 * 1024 /* 1 MiB */),
                        new HTTPClientHandler(request, responseHandler, errorHandler)
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
    
    public HTTPCheckBuilder check()
    {
        return new HTTPCheckBuilder() {
            @Override
            protected void submit(String address, int port, int connectTimeout, int requestTimeout, boolean ssl, boolean permitInvalidCerts, FullHttpRequest request, Consumer<HTTPCheckResponse> responseHandler, Consumer<Throwable> errorHandler)
            {
                HTTPChecker.this.submit(address, port, connectTimeout, requestTimeout, ssl, permitInvalidCerts, request, responseHandler, errorHandler);
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
    
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        HTTPChecker hc = new HTTPChecker();
        //
        hc.check().connect("www.bbc.co.uk").get("/").execute(System.out::println, System.out::println);
        hc.check().connect("intrbiz.com").https().get("/").execute(System.out::println, System.out::println);
    }
}

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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;

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
    private SSLEngine createSSLEngine(boolean permitInvalidCerts, String host, int port, List<String> ciphers)
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
            SSLEngine sslEngine = context.createSSLEngine(host, port);
            sslEngine.setUseClientMode(true);
            // protocols
            sslEngine.setEnabledProtocols(TLSConstants.PROTOCOLS.SAFE_PROTOCOLS);
            // enabled ciphers
            if (ciphers == null || ciphers.isEmpty())
            {
                sslEngine.setEnabledCipherSuites(TLSConstants.getCipherNames(TLSConstants.CIPHERS.SAFE_CIPHERS));
            }
            else
            {
                sslEngine.setEnabledCipherSuites(ciphers.toArray(new String[0]));
            }
            // enable checking that the hostname matches the cert
            if (! permitInvalidCerts)
            {
                SSLParameters params = new SSLParameters();
                params.setEndpointIdentificationAlgorithm("HTTPS");
                sslEngine.setSSLParameters(params);
            }
            return sslEngine;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Failed to init SSLEngine", e);
        }
    }
    
    protected void submit(final String host, final int port, final int connectTimeout, final int requestTimeout, final boolean ssl, final boolean permitInvalidCerts, final String SNIHost, final List<String> ciphers, final FullHttpRequest request, final Consumer<HTTPCheckResponse> responseHandler, final Consumer<Throwable> errorHandler)
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
                // Create the SSL Engine
                SSLEngine sslEngine = ssl ? createSSLEngine(permitInvalidCerts, SNIHost, port, ciphers) : null;
                // Timeouts
                ch.pipeline().addLast(
                        new ReadTimeoutHandler(  requestTimeout < 0 ? HTTPChecker.this.defaultRequestTimeoutSeconds : requestTimeout /* seconds */ ), 
                        new WriteTimeoutHandler( requestTimeout < 0 ? HTTPChecker.this.defaultRequestTimeoutSeconds : requestTimeout /* seconds */ )
                );
                // SSL support
                if (ssl) ch.pipeline().addLast(new SslHandler(sslEngine));
                // HTTP handling
                ch.pipeline().addLast(
                        new HttpClientCodec(),
                        new HttpContentDecompressor(),
                        new HttpObjectAggregator(1 * 1024 * 1024 /* 1 MiB */),
                        new HTTPClientHandler(sslEngine, request, responseHandler, errorHandler)
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
            protected void submit(String address, int port, int connectTimeout, int requestTimeout, boolean ssl, boolean permitInvalidCerts, String SNIHost, List<String> ciphers, FullHttpRequest request, Consumer<HTTPCheckResponse> responseHandler, Consumer<Throwable> errorHandler)
            {
                HTTPChecker.this.submit(address, port, connectTimeout, requestTimeout, ssl, permitInvalidCerts, SNIHost, ciphers, request, responseHandler, errorHandler);
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

package com.intrbiz.bergamot.check.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
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
import io.netty.util.concurrent.GenericFutureListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.crypto.util.BergamotTrustManager;
import com.intrbiz.bergamot.crypto.util.TLSConstants;
import com.intrbiz.util.IBThreadFactory;

/**
 * A non-blocking, asynchronous HTTP checker
 */
public class HTTPChecker
{   
    private final Logger logger = Logger.getLogger(HTTPChecker.class);
    
    private final SSLContext defaultContext;
    
    private final SSLContext badCertContext;
    
    private EventLoopGroup eventLoop;

    private int defaultRequestTimeoutSeconds;

    private int defaultConnectTimeoutSeconds;
    
    private final Timer timer;

    public HTTPChecker(int threads, int defaultConnectTimeoutSeconds, int defaultRequestTimeoutSeconds)
    {
        this.defaultRequestTimeoutSeconds = defaultRequestTimeoutSeconds;
        this.defaultConnectTimeoutSeconds = defaultConnectTimeoutSeconds;
        // setup ssl contexts
        this.defaultContext = this.createContext(false);
        this.badCertContext = this.createContext(true);
        // setup the Netty event loop
        this.eventLoop = new NioEventLoopGroup(threads, new IBThreadFactory("bergamot-http-checker", false));
        // timer
        this.timer = new Timer();
        // some util timer tasks
        // purge the timer queue we cancel most task (hopefully)
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                // purge the timer queue
                HTTPChecker.this.timer.purge();
            }
        }, 180_000L, 180_000L);
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
            // we want to invalidate sessions as soon as possible, to avoid reuse
            context.getClientSessionContext().setSessionTimeout(1);
            context.getServerSessionContext().setSessionTimeout(1);
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
    private SSLEngine createSSLEngine(boolean permitInvalidCerts, String host, int port, List<String> protocols, List<String> ciphers)
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
            // enabled protocols
            if (protocols == null || protocols.isEmpty())
            {
                sslEngine.setEnabledProtocols(TLSConstants.PROTOCOLS.SAFE_PROTOCOLS);    
            }
            else
            {
                sslEngine.setEnabledProtocols(protocols.toArray(new String[0]));
            }
            // enabled ciphers
            if (ciphers == null || ciphers.isEmpty())
            {
                // default to safe ciphers which are supported by this JVM
                sslEngine.setEnabledCipherSuites(TLSConstants.CIPHERS.SAFE_SUPPORTED_CIPHER_NAMES);
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
    
    protected void submit(
            final String host, 
            final int port, 
            final int connectTimeout, 
            final int requestTimeout, 
            final boolean ssl, 
            final boolean permitInvalidCerts, 
            final String SNIHost,
            final List<String> protocols,
            final List<String> ciphers, 
            final FullHttpRequest request, 
            final Consumer<HTTPCheckResponse> responseHandler, 
            final Consumer<Throwable> errorHandler
    )
    {
        // build the url
        String url = (ssl ? "https" : "http") + "://" + host + (port > 0 ? ":" + port : "") + "" + request.uri();
        // configure the client
        Bootstrap b = new Bootstrap();
        b.group(this.eventLoop);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false));
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(connectTimeout < 0 ? this.defaultConnectTimeoutSeconds : connectTimeout));
        b.handler(new ChannelInitializer<SocketChannel>()
        {
            @Override
            public void initChannel(SocketChannel ch) throws Exception
            {
                // Create the SSL Engine
                SSLEngine sslEngine = ssl ? createSSLEngine(permitInvalidCerts, SNIHost, port, protocols, ciphers) : null;
                // SSL support
                if (ssl) ch.pipeline().addLast(new SslHandler(sslEngine));
                // HTTP handling
                ch.pipeline().addLast(
                        new HttpClientCodec(),
                        new HttpContentDecompressor(),
                        new HttpObjectAggregator(1 * 1024 * 1024 /* 1 MiB */),
                        new HTTPClientHandler(url, HTTPChecker.this.timer, sslEngine, request, responseHandler, errorHandler)
                );
            }
        });
        // connect the client
        b.connect(host, port).addListener(new GenericFutureListener<ChannelFuture>()
        {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if (logger.isTraceEnabled()) 
                    logger.trace("Connection to " + host + ":" + port + " " + request.headers().get("Host"));
                if (future.isDone() && (!future.isSuccess()))
                {
                    if (errorHandler != null)
                        errorHandler.accept(future.cause());
                }
            }
        });
    }
    
    public HTTPCheckBuilder get(String url) throws MalformedURLException
    {
        return this.check().get(new URL(url));
    }
    
    public HTTPCheckBuilder post(String url) throws MalformedURLException
    {
        return this.check().post(new URL(url));
    }
    
    public HTTPCheckBuilder head(String url) throws MalformedURLException
    {
        return this.check().head(new URL(url));
    }
    
    public HTTPCheckBuilder check()
    {
        return new HTTPCheckBuilder() {
            @Override
            protected void submit(
                    String address, 
                    int port, 
                    int connectTimeout, 
                    int requestTimeout, 
                    boolean ssl, 
                    boolean permitInvalidCerts, 
                    String SNIHost,
                    List<String> protocols, 
                    List<String> ciphers, 
                    FullHttpRequest request, 
                    Consumer<HTTPCheckResponse> responseHandler, 
                    Consumer<Throwable> errorHandler
            )
            {
                HTTPChecker.this.submit(
                        address, 
                        port, 
                        connectTimeout, 
                        requestTimeout, 
                        ssl, 
                        permitInvalidCerts, 
                        SNIHost, 
                        protocols, 
                        ciphers, 
                        request, 
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

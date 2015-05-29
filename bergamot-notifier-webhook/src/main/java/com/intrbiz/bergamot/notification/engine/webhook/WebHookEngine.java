package com.intrbiz.bergamot.notification.engine.webhook;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.crypto.util.BergamotTrustManager;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.AbstractNotificationEngine;

public class WebHookEngine extends AbstractNotificationEngine
{
    public static final String NAME = "webhook";

    private Logger logger = Logger.getLogger(WebHookEngine.class);
    
    private SSLContext sslContext;
    
    private EventLoopGroup eventLoop;
    
    private final Timer timer;
    
    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    public WebHookEngine()
    {
        super(NAME);
        // timer used for timeouts
        this.timer = new Timer();
        // every 5 minutes forcefully purge the timer queue
        // we cancel most task (hopefully)
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                // purge the timer queue
                timer.purge();
            }
        }, 300_000L, 300_000L);
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        logger.info("WebHook notifier configured");
        // setup our HTTP engine
        // Setup a custom SSLContext which will not validate certs
        this.sslContext = SSLContext.getInstance("TLS");
        this.sslContext.init(null, new TrustManager[] { new BergamotTrustManager(false) }, new SecureRandom());
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

    @Override
    public void sendNotification(Notification notification)
    {
        // we only handle check notifications
        if (notification instanceof CheckNotification)
        {
            CheckNotification checkNotification = (CheckNotification) notification;
            CheckMO check = checkNotification.getCheck();
            // the webhook URL is stored as a check parameter
            String url = check.getParameter("webhook.url");
            if (! Util.isEmpty(url))
            {
                try
                {
                    // parse the URL
                    URL webHookURL = new URL(url);
                    // serialise the message to send
                    byte[] body = this.transcoder.encodeAsBytes(checkNotification);
                    // build the request to send
                    FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, webHookURL.getPath(), Unpooled.wrappedBuffer(body));
                    request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
                    request.headers().add(HttpHeaders.Names.HOST, webHookURL.getPort() == -1 ? webHookURL.getHost() : webHookURL.getHost() + ":" + webHookURL.getPort());
                    request.headers().add(HttpHeaders.Names.USER_AGENT, "Bergamot Monitoring WebHook 1.1.0");
                    request.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=utf-8");
                    // make a HTTP request
                    Bootstrap b = new Bootstrap();
                    b.group(this.eventLoop);
                    b.channel(NioSocketChannel.class);
                    b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(60));
                    b.handler(new ChannelInitializer<SocketChannel>()
                    {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception
                        {
                            // SSL handling
                            SSLEngine sslEngine = null;
                            if ("https".equalsIgnoreCase(webHookURL.getProtocol()))
                            {
                                sslEngine = sslContext.createSSLEngine(webHookURL.getHost(), webHookURL.getPort() == -1 ? 443 : webHookURL.getPort());
                                sslEngine.setUseClientMode(true);
                                SSLParameters params = new SSLParameters();
                                params.setEndpointIdentificationAlgorithm("HTTPS");
                                sslEngine.setSSLParameters(params);
                                ch.pipeline().addLast(new SslHandler(sslEngine));
                            }
                            // HTTP handling
                            ch.pipeline().addLast(
                                    new HttpClientCodec(),
                                    new HttpContentDecompressor(),
                                    new HttpObjectAggregator(1 * 1024 * 1024 /* 1 MiB */),
                                    new WebHookClientHandler(timer, sslEngine, request)
                            );
                        }
                    });
                    // connect the client
                    b.connect(webHookURL.getHost(), webHookURL.getPort() == -1 ? webHookURL.getDefaultPort() : webHookURL.getPort());
                }
                catch (MalformedURLException e)
                {
                    logger.error("Cannot send WebHook to malformed url", e);
                }
            }
        }
    }
}

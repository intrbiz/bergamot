package com.intrbiz.bergamot.notification.engine.slack;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.BergamotVersion;
import com.intrbiz.bergamot.accounting.model.SendNotificationToContactAccountingEvent;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.engine.slack.express.SlackEncode;
import com.intrbiz.bergamot.notification.engine.slack.io.SlackClientHandler;
import com.intrbiz.bergamot.notification.engine.slack.model.SlackMessage;
import com.intrbiz.bergamot.notification.template.NotificationException;
import com.intrbiz.bergamot.notification.template.TemplatedNotificationEngine;
import com.intrbiz.express.ExpressContext;
import com.intrbiz.express.template.ExpressTemplate;
import com.intrbiz.util.IBThreadFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;

public class SlackEngine extends TemplatedNotificationEngine
{
    public static final String NAME = "slack";

    private static final Logger logger = Logger.getLogger(SlackEngine.class);
    
    private SSLContext sslContext;
    
    private EventLoopGroup eventLoop;
    
    private final Timer timer;
    
    private Accounting accounting = Accounting.create(SlackEngine.class);

    public SlackEngine()
    {
        super(BergamotVersion.NAME, NAME, true);
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
    protected void doPrepare(NotificationEngineContext engineContext) throws Exception
    {
        super.doPrepare(engineContext);
        // setup our custom functions for templating
        this.expressExtensions.addFunction("slack_encode", SlackEncode.class);
        // setup our HTTP engine
        this.sslContext = SSLContext.getInstance("TLS");
        this.sslContext.init(null, null, new SecureRandom());
        // setup the Netty event loop
        this.eventLoop = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() + 2, new IBThreadFactory("bergamot-slack-notifier", false));
        // log
        logger.info("Slack notifier configured");
    }
    
    @Override
    protected void doShutdown(NotificationEngineContext engineContext)
    {
        try
        {
            this.eventLoop.shutdownGracefully().await();
        }
        catch (InterruptedException e)
        {
        }
    }
    
    protected void buildMessage(CheckNotification notification, SlackMessage message) throws Exception
    {
        String templateName = notification.getCheck().getCheckType() + "." + notification.getNotificationType();
        ExpressContext context = this.createContext(notification);
        // add the slack message to the context
        context.setEntity("slack_message", message, null);
        // load the template
        ExpressTemplate template = this.templateLoader.load(context, templateName);
        if (template == null) throw new NotificationException("Failed to find template: " + templateName);
        // process the template
        message.rawText(template.encodeToString(context, notification));
        if (logger.isTraceEnabled()) 
            logger.trace(message.toString());
    }

    @Override
    public boolean accept(Notification notification)
    {
        if (notification instanceof CheckNotification)
        {
            return ! this.findNotificationParameters((CheckNotification) notification, "slack.url").isEmpty();
        }
        return false;
    }

    @Override
    public void sendNotification(Notification notification)
    {
        // the slack URL is stored as a parameter on either: contacts, teams, hosts or services
        for (String slackUrl : this.findNotificationParameters((CheckNotification) notification, "slack.url"))
        {
            try
            {
                // parse the URL
                URL url = new URL(slackUrl);
                // build the message to send
                SlackMessage slackMessage = new SlackMessage()
                    .username("Bergamot Monitoring")
                    .iconUrl("https://github.com/intrbiz/bergamot-site/raw/master/src/main/public/logo/disk/64/bergamot_disk_64.png");
                // optionally set the channel
                if (! Util.isEmpty(url.getRef()))
                {
                    if (logger.isDebugEnabled()) logger.debug("Sending slack message to channel: " + url.getRef());
                    slackMessage.channel(url.getRef());
                }
                // template the message
                this.buildMessage((CheckNotification) notification, slackMessage);
                // build the request to send
                FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url.getPath(), Unpooled.wrappedBuffer(slackMessage.toBytes()));
                request.headers().add(HttpHeaderNames.HOST, url.getPort() == -1 ? url.getHost() : url.getHost() + ":" + url.getPort());
                request.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                request.headers().add(HttpHeaderNames.USER_AGENT, "Bergamot Monitoring Slack Notifier 1.0.0");
                request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
                request.headers().add(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                // make a HTTP request
                Bootstrap b = new Bootstrap();
                b.group(this.eventLoop);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false));
                b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(5));
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception
                    {
                        // Force SSL handling
                        SSLEngine sslEngine = null;
                        sslEngine = sslContext.createSSLEngine(url.getHost(), url.getPort() == -1 ? 443 : url.getPort());
                        sslEngine.setUseClientMode(true);
                        SSLParameters params = new SSLParameters();
                        params.setEndpointIdentificationAlgorithm("HTTPS");
                        sslEngine.setSSLParameters(params);
                        ch.pipeline().addLast(new SslHandler(sslEngine));
                        // HTTP handling
                        ch.pipeline().addLast(
                                new HttpClientCodec(),
                                new HttpContentDecompressor(),
                                new HttpObjectAggregator(1 * 1024 * 1024 /* 1 MiB */),
                                new SlackClientHandler(timer, sslEngine, request)
                        );
                    }
                });
                // connect the client
                b.connect(url.getHost(), url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
                // accounting
                this.accounting.account(new SendNotificationToContactAccountingEvent(
                    notification.getSite().getId(),
                    notification.getId(),
                    getObjectId(notification),
                    getNotificationType(notification),
                    null,
                    this.getName(),
                    "slack",
                    url.toString(),
                    null
                ));
            }
            catch (MalformedURLException e)
            {
                logger.error("Cannot send Slack notification to malformed url", e);
            }
            catch (Exception e)
            {
                logger.error("Failed to send Slack notification", e);
            }
        }
    }
}

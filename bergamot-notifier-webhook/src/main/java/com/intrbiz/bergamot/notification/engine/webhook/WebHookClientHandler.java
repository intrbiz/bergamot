package com.intrbiz.bergamot.notification.engine.webhook;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;

import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;

public class WebHookClientHandler extends ChannelInboundHandlerAdapter
{
    private final Logger logger = Logger.getLogger(WebHookClientHandler.class);
    
    private volatile long start;
    
    private final FullHttpRequest request;
    
    private final Timer timer;
    
    private final SSLEngine sslEngine;
    
    private volatile TimerTask timeoutTask;
    
    private volatile boolean timedOut = false;
    
    public WebHookClientHandler(Timer timer, SSLEngine sslEngine, FullHttpRequest request)
    {
        super();
        this.request = request;
        this.timer = timer;
        this.sslEngine = sslEngine;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        if (! this.timedOut)
        {
            FullHttpResponse response = (FullHttpResponse) msg;
            long runtime = System.currentTimeMillis() - this.start;
            logger.info("WebHook: Got HTTP response: " + response.getStatus() + " in: " + runtime + "ms");
            if (logger.isTraceEnabled()) logger.trace("Response:\n" + response);
            // cancel the timeout
            if (this.timeoutTask != null) this.timeoutTask.cancel();
            // Note: we don't currently care what the response it, simply fire and move on
        }
        // close
        if (ctx.channel().isActive()) ctx.close();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        logger.debug("Connection opened: " + ctx.channel().remoteAddress());
        if (this.sslEngine == null)
        {
            // plain request
            this.sendRequest(ctx);
        }
    }
    
    public void userEventTriggered(ChannelHandlerContext ctx, Object event)
    {
        if (this.sslEngine != null && event instanceof SslHandshakeCompletionEvent)
        {
            SslHandshakeCompletionEvent ev = (SslHandshakeCompletionEvent) event;
            logger.debug("SSL handshake complete: " + ev.isSuccess());
            if (ev.isSuccess())
            {
                this.sendRequest(ctx);
            }
        }
    }
    
    protected void sendRequest(ChannelHandlerContext ctx)
    {
        logger.debug("Sending HTTP request");
        this.start = System.currentTimeMillis();
        // schedule timeout for 60 seconds
        this.timeoutTask = new TimeoutTask(this);
        this.timer.schedule(this.timeoutTask, this.start + 60_000L);
        // send the request
        if (logger.isTraceEnabled()) logger.trace("Request:\n" + this.request);
        ctx.writeAndFlush(this.request);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        logger.debug("Connection closed: " + ctx.channel().remoteAddress());
        if (this.timeoutTask != null) this.timeoutTask.cancel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.error("Error processing HTTP request: " + cause);
    }
    
    protected void onTimeout()
    {
        // invoke the error timeout
        this.timedOut = true;
    }
    
    /**
     * Timeout timer task which will nullify the HTTPClientHandler immediately
     */
    protected static class TimeoutTask extends TimerTask
    {
        private volatile WebHookClientHandler handler;
        
        public TimeoutTask(WebHookClientHandler handler)
        {
            this.handler = handler;
        }
        
        @Override
        public void run()
        {
            if (this.handler != null) this.handler.onTimeout();
            this.handler = null;
        }
        
        @Override
        public boolean cancel()
        {
            this.handler = null;
            return super.cancel();
        }
    };
}

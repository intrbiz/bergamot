package com.intrbiz.bergamot.proxy.client.handler;

import java.io.EOFException;
import java.net.URI;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.proxy.model.AuthenticationKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.concurrent.Promise;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = Logger.getLogger(WebSocketHandler.class);
    
    private static final long AGENT_PING_INTERVAL_MS = 30L * 1000L;
    
    private final Timer timer;
    
    private final URI server;
    
    private final ClientHeader client;
    
    private final AuthenticationKey key;
    
    private final Promise<Channel> connectPromise;
    
    private WebSocketClientHandshaker handshaker;
    
    private TimerTask pingTask;
    
    public WebSocketHandler(Timer timer, URI server, ClientHeader client, AuthenticationKey key, Promise<Channel> connectPromise)
    {
        super();
        this.timer = Objects.requireNonNull(timer);
        this.server = Objects.requireNonNull(server);
        this.client = Objects.requireNonNull(client);
        this.key = Objects.requireNonNull(key);
        this.connectPromise = Objects.requireNonNull(connectPromise);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        if (logger.isTraceEnabled()) logger.trace("Connected, starting handshake");
        // Start the WS connection
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(this.server, WebSocketVersion.V13, null, false, this.client.sign(this.key).toHeaders());
        this.handshaker.handshake(ctx.channel());
    }

    public void channelHandshaked(ChannelHandlerContext ctx)
    {
        if (logger.isTraceEnabled()) logger.trace("Handshake done");
        // schedule ping
        final Channel channel = ctx.channel();
        this.pingTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (channel.isActive())
                {
                    if (logger.isTraceEnabled()) logger.trace("Sending ping to server");
                    channel.writeAndFlush(new PingWebSocketFrame());
                }
            }
        };
        this.timer.scheduleAtFixedRate(this.pingTask, AGENT_PING_INTERVAL_MS, AGENT_PING_INTERVAL_MS);
        // fire promise
        this.connectPromise.setSuccess(channel);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof FullHttpResponse)
        {
            FullHttpResponse httpResponse = (FullHttpResponse) msg;
            if (logger.isDebugEnabled())  logger.debug("Got response: " + httpResponse);
            // Check the response status
            // Complete the handshake
            if (! handshaker.isHandshakeComplete())
            {
                handshaker.finishHandshake(ctx.channel(), httpResponse);
                if (handshaker.isHandshakeComplete())
                {
                    this.channelHandshaked(ctx);
                }
                else
                {
                    throw new IllegalStateException("WebSocket handshake failed");
                }
            }
        }
        else if (msg instanceof WebSocketFrame && handshaker.isHandshakeComplete())
        {
            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof PongWebSocketFrame)
            {
                if (logger.isTraceEnabled()) logger.trace("Got pong from server");
            }
            else if (frame instanceof PingWebSocketFrame)
            {
                if (logger.isTraceEnabled()) logger.trace("Got ping from server");
                ctx.channel().writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content().retain()));
            }
            else if (frame instanceof CloseWebSocketFrame)
            {
                if (logger.isTraceEnabled()) logger.trace("Closing connection");
                ctx.close();
            }
            else
            {
                ctx.fireChannelRead(frame.retain());
            }
        }
        else
        {
            throw new IllegalStateException("Unexpected message, got: " + msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
    {
        if (logger.isTraceEnabled()) logger.trace("Unhandled error communicating with Bergamot proxy server", e);
        if (! this.connectPromise.isDone())
        {
            this.connectPromise.setFailure(e);
        }
        ctx.close();
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        if (this.pingTask != null)
        {
            this.pingTask.cancel();
        }
        if (! this.connectPromise.isDone())
        {
            this.connectPromise.setFailure(new EOFException());
        }
        super.channelInactive(ctx);
    }    
}
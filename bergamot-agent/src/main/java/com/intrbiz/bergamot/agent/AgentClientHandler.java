package com.intrbiz.bergamot.agent;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.util.AgentUtil;
import com.intrbiz.bergamot.io.BergamotAgentTranscoder;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

public abstract class AgentClientHandler extends SimpleChannelInboundHandler<Object>
{    
    private static final long AGENT_PING_INTERVAL_MS = 30L * 1000L;
    
    private Logger logger = Logger.getLogger(AgentClientHandler.class);

    private final URI server;
    
    private final UUID agentId;
    
    private final String hostName;
    
    private final String templateName;
    
    private final AgentAuthenticationKey key;
    
    private final Timer timer;
    
    private final BergamotAgentTranscoder transcoder = BergamotAgentTranscoder.getDefaultInstance();
    
    private WebSocketClientHandshaker handshaker;

    public AgentClientHandler(Timer timer, URI server, UUID agentId, String hostName, String templateName, AgentAuthenticationKey key)
    {
        super();
        this.timer = timer;
        this.server = server;
        this.agentId = agentId;
        this.hostName = hostName;
        this.templateName = templateName;
        this.key = key;
    }
    
    protected abstract AgentMessage processAgentMessage(final ChannelHandlerContext ctx, final AgentMessage request);
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        logger.trace("Connected, starting handshake");
        // Timestamp used for authentication in seconds since unix epoch
        long timestamp = System.currentTimeMillis() / 1000;
        // Headers we send on connect
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set(HttpHeaderNames.USER_AGENT, BergamotAgent.AGENT_VENDOR + "(" + BergamotAgent.AGENT_PRODUCT + ")/" + BergamotAgent.AGENT_VERSION);
        headers.set(AgentHTTPHeaderNames.AGENT_ID, this.agentId.toString());
        headers.set(AgentHTTPHeaderNames.HOST_NAME, this.hostName);
        if (! AgentUtil.isEmpty(this.templateName)) headers.set(AgentHTTPHeaderNames.TEMPLATE_NAME, this.templateName);
        headers.set(AgentHTTPHeaderNames.KEY_ID, this.key.getId().toString());
        headers.set(AgentHTTPHeaderNames.TIMESTAMP, timestamp);
        headers.set(HttpHeaderNames.AUTHORIZATION, this.key.sign(timestamp, this.agentId, this.hostName, this.templateName));
        // Start the WS connection
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(this.server, WebSocketVersion.V13, null, false, headers);
        this.handshaker.handshake(ctx.channel());
    }

    public void channelHandshaked(ChannelHandlerContext ctx)
    {
        logger.trace("Handshake done");
        final Channel channel = ctx.channel();
        // schedule ping
        this.timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                if (channel.isActive())
                {
                    logger.trace("Sending ping to server");
                    channel.writeAndFlush(new TextWebSocketFrame(transcoder.encodeAsString(new AgentPing(UUID.randomUUID().toString(), System.currentTimeMillis()))));
                }
                else
                {
                    this.cancel();
                }
            }
        }, AGENT_PING_INTERVAL_MS, AGENT_PING_INTERVAL_MS);
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, Object msg)
    {
        if (msg instanceof FullHttpResponse)
        {
            FullHttpResponse http = (FullHttpResponse) msg;
            // complete the handshake
            if (!handshaker.isHandshakeComplete())
            {
                handshaker.finishHandshake(ctx.channel(), http);
                this.channelHandshaked(ctx);
                return;
            }
        }
        else if (msg instanceof WebSocketFrame)
        {
            // process the frame
            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame)
            {
                String message = ((TextWebSocketFrame) frame).text();
                if (logger.isDebugEnabled()) logger.debug("Got message from agent server: " + message);
                try
                {
                    final AgentMessage request = this.transcoder.decodeFromString(message, AgentMessage.class);
                    // process the request
                    if (request instanceof AgentMessage)
                    {
                        // process the message
                        ctx.executor().execute(new Runnable() {
                            public void run()
                            {
                                try
                                {
                                    AgentMessage response = processAgentMessage(ctx, request);
                                    // respond
                                    if (response != null)
                                    {
                                        ctx.channel().writeAndFlush(new TextWebSocketFrame(transcoder.encodeAsString(response)));
                                    }
                                }
                                catch (Exception e)
                                {
                                    ctx.channel().writeAndFlush(new TextWebSocketFrame(transcoder.encodeAsString(new GeneralError("Failed to process message: " + e.getMessage()))));
                                }
                            }
                        });
                    }
                    else
                    {
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new GeneralError(request, "Bad request"))));
                    }
                }
                catch (Exception e)
                {
                    logger.error("Failed to decode request", e);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new GeneralError("Failed to decode request"))));
                }
            }
            else if (frame instanceof PongWebSocketFrame)
            {
                logger.trace("Got pong, whoop");
            }
            else if (frame instanceof CloseWebSocketFrame)
            {
                logger.trace("Closing connection");
                ctx.close();
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
        logger.error("Unhandled error communicating with Bergamot server", e);
        ctx.close();
    }
}

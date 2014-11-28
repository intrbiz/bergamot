package com.intrbiz.bergamot.agent.server;

import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.io.BergamotAgentTranscoder;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.hello.AgentHello;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;


public class AgentServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = Logger.getLogger(AgentServerHandler.class);

    private static final String WEBSOCKET_PATH = "/agent";
    
    private final AgentServer server;

    private WebSocketServerHandshaker handshaker;
    
    private final BergamotAgentTranscoder transcoder = BergamotAgentTranscoder.getDefaultInstance();
    
    private AgentHello hello;
    
    private SocketAddress remoteAddress;
    
    private Channel channel;
    
    private ConcurrentMap<String, Consumer<AgentMessage>> pendingRequests = new ConcurrentHashMap<String, Consumer<AgentMessage>>();
    
    public AgentServerHandler(AgentServer server)
    {
        super();
        this.server = server;
    }
    
    public AgentHello getHello()
    {
        return this.hello;
    }
    
    public SocketAddress getRemoteAddress()
    {
        return this.remoteAddress;
    }
    
    public Channel getChannel()
    {
        return this.channel;
    }
    
    public void sendMessageToAgent(AgentMessage message, Consumer<AgentMessage> onResponse)
    {
        // ensure the message has an id
        if (message.getId() == null) message.setId(UUID.randomUUID().toString());
        // stash the message
        this.pendingRequests.put(message.getId(), onResponse);
        // send the message
        this.channel.writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(message)));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        this.channel = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // unregister this agent
        if (this.hello != null)
        {
            this.server.unregisterAgent(this);
        }
        // invoke any pending messages
        for (Consumer<AgentMessage> callback : this.pendingRequests.values())
        {
            callback.accept(new GeneralError("Channel closed"));
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof FullHttpRequest)
        {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
        else if (msg instanceof WebSocketFrame)
        {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception
    {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess())
        {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        // Handshake
        logger.trace("Handshaking websocket request url: " + req.getUri());
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
        this.handshaker = wsFactory.newHandshaker(req);
        if (this.handshaker == null)
        {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        }
        else
        {
            this.handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(final ChannelHandlerContext ctx, WebSocketFrame frame)
    {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame)
        {
            this.handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // ping pong
        if (frame instanceof PingWebSocketFrame)
        {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // only support text frames
        if (!(frame instanceof TextWebSocketFrame)) throw new IllegalStateException(frame.getClass().getName() + " frame types not supported");
        // get the frame
        try
        {
            AgentMessage request = this.transcoder.decodeFromString(((TextWebSocketFrame) frame).text(), AgentMessage.class);
            // process the message and respond
            AgentMessage response = this.processMessage(ctx, request);
            if (response != null)
            {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(response)));
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to decode request", e);
            ctx.close();
        }
    }
    
    private AgentMessage processMessage(final ChannelHandlerContext ctx, final AgentMessage request)
    {
        if (request instanceof AgentHello)
        {
            this.hello = (AgentHello) request;
            this.remoteAddress = ctx.channel().remoteAddress();
            logger.info("Got hello from " + this.remoteAddress + " " + this.hello.toString());
            // register ourselves
            this.server.registerAgent(this);
            // no response
            return null;
        }
        else if (request instanceof AgentPing)
        {
            logger.debug("Got ping from agent");
            return new AgentPong(UUID.randomUUID().toString());
        }
        else if (request instanceof AgentPong)
        {
            logger.debug("Got pong from agent");
            return null;
        }
        else
        {
            // do we have a callback to invoke
            Consumer<AgentMessage> callback = this.pendingRequests.remove(request.getId());
            if (callback != null)
            {
                callback.accept(request);
                return null;
            }
        }
        logger.warn("Unhandled message: " + request);
        return null;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res)
    {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200)
        {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }
        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200)
        {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.error("Error processing request", cause);
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req)
    {
        return "wss://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }
}
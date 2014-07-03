package com.intrbiz.bergamot.updater;

import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.api.APIObject;
import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.api.APIResponse;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.update.RegisterForUpdates;
import com.intrbiz.bergamot.model.message.api.update.RegisteredForUpdates;
import com.intrbiz.bergamot.model.message.api.update.UpdateEvent;
import com.intrbiz.bergamot.model.message.api.util.APIPing;
import com.intrbiz.bergamot.model.message.api.util.APIPong;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueException;


/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class);

    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;

    private UpdateQueue queue;

    private Consumer<Update> updateConsumer;

    private BergamotTranscoder transcoder = new BergamotTranscoder();
    
    public WebSocketServerHandler()
    {
        super();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // shutdown the queues
        if (this.updateConsumer != null) this.updateConsumer.close();
        if (this.queue != null) this.queue.close();
        // super
        super.channelInactive(ctx);
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
        logger.trace("Handshaking websocket request Origin: " + req.headers().get("Origin") + " url: " + req.getUri());
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
        // only suppor text frames
        if (!(frame instanceof TextWebSocketFrame)) throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        // get the frame
        try
        {
            APIObject request = this.transcoder.decodeFromString(((TextWebSocketFrame) frame).text(), APIObject.class);
            // process the request
            if (request instanceof APIRequest)
            {
                APIResponse response = this.processAPIRequest(ctx, (APIRequest) request);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(response)));
            }
            else
            {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new APIError("Bad request"))));
                // TODO close
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to decode request", e);
            // send an error response
            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new APIError("Failed to decode request"))));
            // TODO close
        }
    }
    
    private APIResponse processAPIRequest(final ChannelHandlerContext ctx, APIRequest request)
    {
        if (request instanceof APIPing)
        {
            APIPing ping = (APIPing) request;
            logger.trace("Returning pong");
            return new APIPong(ping);
        }
        else if (request instanceof RegisterForUpdates)
        {
            RegisterForUpdates rfsn = (RegisterForUpdates) request;
            // setup the queue
            if (this.queue == null || this.updateConsumer == null)
            {
                logger.info("Reigster for updates, for checks: " + rfsn.getCheckIds());
                //
                Set<String> bindings = new HashSet<String>();
                for (UUID checkId : rfsn.getCheckIds())
                {
                    bindings.add(Site.getSiteId(checkId).toString() + "." + checkId.toString());
                }
                //
                try
                {
                    this.queue = UpdateQueue.open();
                    this.updateConsumer = this.queue.consumeUpdates((u) -> {
                        // send update to client
                        // logger.info("Got update from queue: " + u);
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(transcoder.encodeAsString(new UpdateEvent(u))));
                    }, bindings);
                }
                catch (QueueException e)
                {
                    this.queue = null;
                    this.updateConsumer = null;
                    logger.error("Failed to setup queue", e);
                    return new APIError("Failed to setup queue");
                }
            }
            else
            {
                for (UUID checkId : rfsn.getCheckIds())
                {
                    logger.info("Updating bindings: " + checkId);
                    this.updateConsumer.addBinding(Site.getSiteId(checkId).toString() + "." + checkId.toString());
                }
            }
            return new RegisteredForUpdates(rfsn);
        }
        return new APIError("Not found");
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
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }
}
package com.intrbiz.bergamot.agent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;

import org.apache.log4j.Logger;

public class WSClientHandler extends ChannelInboundHandlerAdapter
{
    private Logger logger = Logger.getLogger(WSClientHandler.class);

    private final WebSocketClientHandshaker handshaker;

    public WSClientHandler(URI server)
    {
        super();
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set(HttpHeaders.Names.USER_AGENT, "BergamotAgent/1.0.0 (Java)");
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(server, WebSocketVersion.V13, null, false, headers);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        logger.debug("Connected, starting handshake");
        handshaker.handshake(ctx.channel());
    }

    public void channelHandshaked(ChannelHandlerContext ctx)
    {
        logger.debug("Handshake done");
        ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"type\":\"bergamot.api.util.ping\",\"request_id\":\"testing_12345\"}"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        // complete the handshake
        if (!handshaker.isHandshakeComplete())
        {
            handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
            this.channelHandshaked(ctx);
            return;
        }
        // check we only expect a websocket frame
        if (msg instanceof WebSocketFrame)
        {
            // process the frame
            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame)
            {
                logger.info("Message: " + ((TextWebSocketFrame) frame).text());
            }
            else if (frame instanceof PongWebSocketFrame)
            {
                logger.debug("Got pong, whoop");
            }
            else if (frame instanceof CloseWebSocketFrame)
            {
                logger.debug("Closing connection");
                ctx.close();
            }
        }
        else
        {
            throw new IllegalStateException("Expected WebSocketFrame, got: " + msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
    {
        logger.error("Unhandled error communicating with Bergamot server", e);
        ctx.close();
    }
}

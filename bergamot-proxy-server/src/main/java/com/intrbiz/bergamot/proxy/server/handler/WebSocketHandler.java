package com.intrbiz.bergamot.proxy.server.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.proxy.KeyResolver;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AsciiString;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = Logger.getLogger(WebSocketHandler.class);

    private static final Charset UTF8 = Charset.forName("UTF8");

    private static final String TEXT_PLAIN = "text/plain; charset=utf-8";

    private static final byte[] BAD_REQUEST_CONTENT = "Bad request\r\n".getBytes(UTF8);

    private static final byte[] FORBIDDEN_CONTENT = "Bad authentication\r\n".getBytes(UTF8);

    private final AsciiString serverName;

    private final String websocketPath;

    private final KeyResolver keyResolver;

    private WebSocketServerHandshaker handshaker;

    private volatile boolean handshaked = false;

    private volatile boolean authenticated = false;

    private ClientHeader clientHeader;

    public WebSocketHandler(String websocketPath, String serverName, KeyResolver keyResolver)
    {
        super();
        this.serverName = AsciiString.cached(Objects.requireNonNull(serverName));
        this.websocketPath = Objects.requireNonNull(websocketPath);
        this.keyResolver = Objects.requireNonNull(keyResolver);
    }

    public boolean isHandshaked()
    {
        return this.handshaked;
    }

    public boolean isAuthenticated()
    {
        return this.authenticated;
    }

    public ClientHeader getClientHeader()
    {
        return this.clientHeader;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        if (msg instanceof FullHttpRequest)
        {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
        else if (msg instanceof WebSocketFrame && this.authenticated && this.handshaked)
        {
            if (msg instanceof CloseWebSocketFrame)
            {
                this.handshaker.close(ctx.channel(), ((CloseWebSocketFrame) msg).retain());
                return;
            }
            if (msg instanceof PingWebSocketFrame)
            {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content().retain()));
                return;
            }
            else
            {
                // Fire other frames down the pipeline
                ctx.fireChannelRead(msg);
            }
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception
    {
        if (req.decoderResult().isSuccess())
        {
            // decode the client header
            this.clientHeader = ClientHeader.fromRequest(req, ctx.channel().remoteAddress());
            // authenticate the client
            this.clientHeader.authenticate(this.keyResolver).thenAccept((authResult) -> {
                if (authResult)
                {
                    this.authenticated = true;
                    WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(this.getWebSocketLocation(req), null, false);
                    this.handshaker = wsFactory.newHandshaker(req);
                    if (this.handshaker == null)
                    {
                        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                    }
                    else
                    {
                        this.handshaker.handshake(ctx.channel(), req).addListener((future) -> {
                            if (future.isDone() && future.isSuccess())
                            {
                                // Allow WebSocket traffic to flow
                                this.handshaked = true;
                                // Fire connected event
                                ctx.fireUserEventTriggered(this.clientHeader);
                            }
                        });
                    }
                }
                else
                {
                    try
                    {
                        ctx.writeAndFlush(this.buildResponse(FORBIDDEN, Unpooled.wrappedBuffer(FORBIDDEN_CONTENT))).addListener(ChannelFutureListener.CLOSE);
                    }
                    catch (Exception e)
                    {
                        ctx.fireExceptionCaught(e);
                    }
                }
            });
        }
        else
        {
            ctx.writeAndFlush(this.buildResponse(BAD_REQUEST, Unpooled.wrappedBuffer(BAD_REQUEST_CONTENT))).addListener(ChannelFutureListener.CLOSE);
        }
    }

    protected DefaultFullHttpResponse buildResponse(HttpResponseStatus status, ByteBuf content) throws Exception
    {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content, false, false);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        response.headers().set(HttpHeaderNames.SERVER, this.serverName);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, TEXT_PLAIN);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        logger.error("Error processing request", cause);
        ctx.close();
    }

    private String getWebSocketLocation(FullHttpRequest req)
    {
        return "wss://" + req.headers().get(HttpHeaderNames.HOST) + this.websocketPath;
    }
}
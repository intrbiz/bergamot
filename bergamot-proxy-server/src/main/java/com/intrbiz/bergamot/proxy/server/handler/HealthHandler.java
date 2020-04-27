package com.intrbiz.bergamot.proxy.server.handler;

import java.nio.charset.Charset;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;

@Sharable
public class HealthHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{
    private static final String HEALTH_CHECK = "/health";
    
    private static final String HEALTH_CHECK_ALIVE = HEALTH_CHECK + "/alive";
    
    private static final String HEALTH_CHECK_READY = HEALTH_CHECK + "/ready";
    
    private static final Charset UTF8 = Charset.forName("UTF8");
    
    private static final String TEXT_PLAIN = "text/plain; charset=utf-8";
    
    private static final byte[] ALIVE_CONTENT = "OK\r\n".getBytes(UTF8);
    
    private static final byte[] UNKNOWN_CONTENT = "Unknown Healthcheck\r\n".getBytes(UTF8);
    
    private final AsciiString serverName;
    
    public HealthHandler(String serverName)
    {
        super();
        this.serverName = AsciiString.cached(Objects.requireNonNull(serverName));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception
    {
        String path = req.uri();
        if (path != null && path.startsWith(HEALTH_CHECK))
        {
            ctx.writeAndFlush(this.handleHealthCheck(req))
                .addListener(ChannelFutureListener.CLOSE);
        }
        else
        {
            ctx.fireChannelRead(req.retain());
        }
    }
    
    protected DefaultFullHttpResponse handleHealthCheck(FullHttpRequest request) throws Exception {
        switch (request.uri()) {
            case HEALTH_CHECK_ALIVE:
                return this.handleAlive(request);
            case HEALTH_CHECK_READY:
                return this.handleReady(request);
        }
        return this.handleUnknown(request);
    }
    
    protected DefaultFullHttpResponse handleAlive(FullHttpRequest request) throws Exception {
        return this.buildResponse(HttpResponseStatus.OK, Unpooled.wrappedBuffer(ALIVE_CONTENT));
    }
    
    protected DefaultFullHttpResponse handleReady(FullHttpRequest request) throws Exception {
        // Currently ready is the same as alive
        return this.handleAlive(request);
    }
    
    protected DefaultFullHttpResponse handleUnknown(FullHttpRequest request) throws Exception {
        return this.buildResponse(HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer(UNKNOWN_CONTENT));
    }
    
    protected DefaultFullHttpResponse buildResponse(HttpResponseStatus status, ByteBuf content) throws Exception {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content, false, false);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        response.headers().set(HttpHeaderNames.SERVER, this.serverName);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, TEXT_PLAIN);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
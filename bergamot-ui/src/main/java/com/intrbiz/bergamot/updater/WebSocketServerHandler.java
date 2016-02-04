package com.intrbiz.bergamot.updater;

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
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.BalsaContext;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.api.APIObject;
import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.updater.context.ClientContext;
import com.intrbiz.bergamot.updater.handler.RequestHandler;
import com.intrbiz.bergamot.updater.util.CookieJar;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class);

    private static final String WEBSOCKET_PATH = "/websocket";

    private final UpdateServer server;

    private ClientContext context;

    private WebSocketServerHandshaker handshaker;

    private BergamotTranscoder transcoder = new BergamotTranscoder();

    public WebSocketServerHandler(UpdateServer server)
    {
        super();
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        final Channel channel = ctx.channel();
        // setup the context
        this.context = new ClientContext()
        {
            @Override
            public void send(APIObject value)
            {
                channel.writeAndFlush(new TextWebSocketFrame(transcoder.encodeAsString(value)));
            }
        };
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // shutdown the context
        if (this.context != null) this.context.close();
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
    
    private void authenticateContext(FullHttpRequest request) throws BalsaSecurityException
    {
        // extract the Balsa session cookie
        CookieJar cookies = CookieJar.parseCookies(HttpHeaders.getHeader(request, HttpHeaders.Names.COOKIE));
        String sessionId = cookies.cookie(BalsaSession.COOKIE_NAME);
        if (Util.isEmpty(sessionId)) 
            throw new BalsaSecurityException("No Blasa session cookie found");
        // lookup the session
        final BalsaApplication application = BalsaApplication.getInstance();
        final BalsaSession     session     = application.getSessionEngine().getSession(sessionId);
        if (session == null) 
            throw new BalsaSecurityException("Invalid session id");
        // lookup the site and principal
        try
        {
            BalsaContext.withContext(application, session, () -> {
                WebSocketServerHandler.this.context.setSite(session.var("site"));
                WebSocketServerHandler.this.context.setPrincipal(session.currentPrincipal());
                return null;
            });
        }
        catch (Exception e)
        {
            if (e instanceof BalsaSecurityException) 
                throw (BalsaSecurityException) e;
            throw new BalsaSecurityException("Failed to get site and principal from session", e);
        }
        // finally validate
        if (this.context.getSite() == null || this.context.getPrincipal() == null)
            throw new BalsaSecurityException("Failed to get site and principal");
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception
    {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess())
        {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        // Authenticate this connection
        try
        {
            this.authenticateContext(req);
            logger.debug("Authenticated websock connection for principal: " + this.context.getPrincipal());
        }
        catch (BalsaSecurityException e)
        {
            logger.warn("Denying access for websocket", e);
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
        // Handshake
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleWebSocketFrame(final ChannelHandlerContext ctx, WebSocketFrame frame)
    {
        if (frame instanceof CloseWebSocketFrame)
        {
            // Check for closing frame
            this.handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        else if (frame instanceof PingWebSocketFrame)
        {
            // ping pong
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        else if (frame instanceof TextWebSocketFrame)
        {
            // only support text frames
            try
            {
                APIObject request = this.transcoder.decodeFromString(((TextWebSocketFrame) frame).text(), APIObject.class);
                // process the request
                if (request instanceof APIRequest)
                {
                    APIRequest apiRequest = (APIRequest) request;
                    // process the request
                    RequestHandler<?> handler = this.server.getHandler(request.getClass());
                    if (handler != null)
                    {
                        try
                        {
                            ((RequestHandler) handler).onRequest(this.context, apiRequest);
                        }
                        catch (Exception e)
                        {
                            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new APIError(apiRequest, e.getMessage()))));            
                        }
                    }
                    else
                    {
                        ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new APIError(apiRequest, "Not found"))));
                    }
                }
                else
                {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new APIError("Bad request"))));
                    ctx.channel().close();
                }
            }
            catch (Exception e)
            {
                logger.error("Failed to decode request", e);
                // send an error response
                ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(new APIError("Failed to decode request"))));
                ctx.channel().close();
            }
        }
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
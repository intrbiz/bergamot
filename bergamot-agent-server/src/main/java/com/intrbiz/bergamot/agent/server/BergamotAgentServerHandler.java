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
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.crypto.util.CertInfo;
import com.intrbiz.bergamot.crypto.util.SerialNum;
import com.intrbiz.bergamot.io.BergamotAgentTranscoder;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.hello.AgentHello;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;


public class BergamotAgentServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static final Logger logger = Logger.getLogger(BergamotAgentServerHandler.class);

    private static final String WEBSOCKET_PATH = "/agent";
    
    private final BergamotAgentServer server;

    private WebSocketServerHandshaker handshaker;
    
    private final BergamotAgentTranscoder transcoder = BergamotAgentTranscoder.getDefaultInstance();
    
    private AgentHello hello;
    
    private SocketAddress remoteAddress;
    
    private Channel channel;
    
    private ConcurrentMap<String, Consumer<AgentMessage>> pendingRequests = new ConcurrentHashMap<String, Consumer<AgentMessage>>();
    
    private final SSLEngine engine;
    
    private Certificate agentCertificate;
    
    private CertInfo agentCertificateInfo;
    
    private Certificate siteCertificate;
    
    private CertInfo siteCertificateInfo;
    
    private UUID agentId;
    
    private UUID siteId;
    
    public BergamotAgentServerHandler(BergamotAgentServer server, SSLEngine engine)
    {
        super();
        this.server = server;
        this.engine = engine;
    }
    
    /**
     * The Agent Id as extracted from the certificate
     */
    public UUID getAgentId()
    {
        return this.agentId;
    }
    
    /**
     * The Site Id as extracted from the site certificate
     */
    public UUID getSiteId()
    {
        return this.siteId;
    }
    
    public CertInfo getAgentCertificateInfo()
    {
        return this.agentCertificateInfo;
    }
    
    public String getAgentName()
    {
        return this.agentCertificateInfo.getSubject().getCommonName();
    }
    
    public CertInfo getSiteCertificateInfo()
    {
        return this.siteCertificateInfo;
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
    
    public void sendOnePingAndOnePingOnly(Consumer<Long> onPong)
    {
        this.sendMessageToAgent(new AgentPing(UUID.randomUUID().toString(), System.currentTimeMillis()), (message) -> onPong.accept(System.currentTimeMillis() - ((AgentPong) message).getTimestamp()) );
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
        // validate the certificate
        if (this.validateAgentCertificate(this.engine.getSession().getPeerPrincipal(), this.engine.getSession().getPeerCertificates()))
        {
            // got a good client certificate, start the WS handshake
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
        else
        {
            // bad client certificate, terminate the connection
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
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
            this.processMessage(ctx, request);
        }
        catch (Exception e)
        {
            logger.error("Failed to decode request", e);
            ctx.close();
        }
    }
    
    private void processMessage(final ChannelHandlerContext ctx, final AgentMessage request) throws Exception
    {
        if (request instanceof AgentHello)
        {
            this.hello = (AgentHello) request;
            this.remoteAddress = ctx.channel().remoteAddress();
            logger.info("Got hello from " + this.remoteAddress + " " + this.agentId + " " + this.agentCertificateInfo.getSubject().getCommonName());
            // register ourselves
            this.server.registerAgent(this);
        }
        else if (request instanceof AgentPing)
        {
            logger.debug("Got ping from agent");
            this.server.fireAgentPing(this);
            writeMessage(ctx, new AgentPong((AgentPing) request));
        }
        else if (request instanceof AgentPong)
        {
            Consumer<AgentMessage> callback = this.pendingRequests.remove(request.getId());
            if (callback != null)
            {
                callback.accept(request);
            }
            else
            {
                logger.debug("Got pong from agent");
            }
        }
        else
        {
            if (request.getId() != null)
            {
                // do we have a callback to invoke
                Consumer<AgentMessage> callback = this.pendingRequests.remove(request.getId());
                if (callback != null)
                {
                    callback.accept(request);
                }
                else
                {
                    logger.warn("Unhandled message: " + request);
                }
            }
            else
            {
                logger.warn("Unhandled message, no request id: " + request);
            }
        }
    }
    
    private void writeMessage(final ChannelHandlerContext ctx, final AgentMessage message) throws Exception
    {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(message)));
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
    
    /**
     * Validate the agent client auth certificate.
     * 
     * The Bergamot Agent encodes and signs important information 
     * into the certificate:
     * 
     * 1) The Agent UUID - encoded in the agent certificate serial number
     * 2) The Agent common name - the common name of the agent certificate
     * 3) The Site  UUID - encoded in the site CA certificate serial number
     * 
     * @param clientPrincipal
     * @param clientCertificates
     */
    private boolean validateAgentCertificate(Principal clientPrincipal, Certificate[] clientCertificates)
    {
        // assert that we have a certificate
        if (clientPrincipal == null || clientCertificates == null || clientCertificates.length < 2)
        {
            logger.debug("Invalid agent certificate chain, not valid!");
            return false;
        }
        try
        {
            // the client auth certificte chain will be:
            // 0 - agent certificate
            // 1 - site authority certificate
            // 2 - root authority certificate
            // agent cert
            this.agentCertificate = clientCertificates[0];
            this.agentCertificateInfo = CertInfo.fromCertificate(this.agentCertificate);
            // site CA cert
            this.siteCertificate = clientCertificates[1];
            this.siteCertificateInfo = CertInfo.fromCertificate(this.siteCertificate);
            // check the serial numbers
            this.agentId = SerialNum.fromBigInt(((X509Certificate) this.agentCertificate).getSerialNumber()).getId();
            this.siteId  = SerialNum.fromBigInt(((X509Certificate) this.siteCertificate).getSerialNumber()).getId();
            // TODO: validate that the Agent Id is masked by the Site Id
            // log
            logger.info("Connection from client: " + this.agentCertificateInfo.getSubject().getCommonName() + " of site " + this.siteCertificateInfo.getSubject().getCommonName());
            return true;
        }
        catch (Exception e)
        {
            logger.error("Error validating client certificate", e);
        }
        return false;
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
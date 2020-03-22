package com.intrbiz.bergamot.agent.server;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.AgentHTTPHeaderNames;
import com.intrbiz.bergamot.io.BergamotAgentTranscoder;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.SiteMO;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.check.CheckDiskIO;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.check.CheckMetrics;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetCon;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIO;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.check.CheckUptime;
import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.check.ShellCheck;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;
import com.intrbiz.bergamot.model.message.agent.util.Parameter;

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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;


public class BergamotAgentServerHandler extends SimpleChannelInboundHandler<Object>
{
    private static final long AUTHENTICATION_GRACE_SECONDS = 30;
    
    private static final Logger logger = Logger.getLogger(BergamotAgentServerHandler.class);

    private static final String WEBSOCKET_PATH = "/agent";
    
    private static final String HEALTH_CHECK = "/health";
    
    private static final String HEALTH_CHECK_ALIVE = HEALTH_CHECK + "/alive";
    
    private static final String HEALTH_CHECK_READY = HEALTH_CHECK + "/ready";
    
    private static final Charset UTF8 = Charset.forName("UTF8");
    
    private static final String TEXT_PLAIN = "text/plain; charset=utf-8";
    
    private static final byte[] ALIVE_CONTENT = "OK\r\n".getBytes(UTF8);
    
    private static final byte[] UNKNOWN_CONTENT = "Unknown Healthcheck\r\n".getBytes(UTF8);
    
    private static final AsciiString SERVER_NAME = AsciiString.cached("Bergamot Agent Server");
    
    private final BergamotAgentServer server;

    private WebSocketServerHandshaker handshaker;
    
    private final BergamotAgentTranscoder transcoder = BergamotAgentTranscoder.getDefaultInstance();
    
    private SocketAddress remoteAddress;
    
    private Channel channel;
    
    private ConcurrentMap<String, Consumer<AgentMessage>> pendingRequests = new ConcurrentHashMap<String, Consumer<AgentMessage>>();
    
    private boolean handshaked = false;
    
    private boolean authenticated = false;
    
    private UUID agentKeyId;
    
    private UUID agentId;
    
    private UUID siteId;
    
    private String agentUserAgent;
    
    private String agentHostName;
    
    private String agentTemplateName;
    
    private long authenticationTimestamp;
    
    public BergamotAgentServerHandler(BergamotAgentServer server)
    {
        super();
        this.server = server;
    }
    
    /**
     * The Agent Id as extracted from the certificate
     */
    public UUID getAgentId()
    {
        return this.agentId;
    }
    
    public UUID getSiteId()
    {
        return this.siteId;
    }
    
    public boolean isHandshaked()
    {
        return this.handshaked;
    }

    public boolean isAuthenticated()
    {
        return this.authenticated;
    }

    public String getAgentUserAgent()
    {
        return this.agentUserAgent;
    }

    public String getAgentHostName()
    {
        return this.agentHostName;
    }

    public String getAgentTemplateName()
    {
        return this.agentTemplateName;
    }

    public long getAuthenticationTimestamp()
    {
        return this.authenticationTimestamp;
    }

    public UUID getAgentKeyId()
    {
        return this.agentKeyId;
    }

    public SocketAddress getRemoteAddress()
    {
        return this.remoteAddress;
    }
    
    public Channel getChannel()
    {
        return this.channel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        this.channel = ctx.channel();
        this.remoteAddress = ctx.channel().remoteAddress();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        // Fire disconnect event
        if (this.authenticated && this.handshaked && this.agentId != null)
        {
            this.server.fireAgentDisconnect(this);
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
        else if (msg instanceof WebSocketFrame && this.handshaked)
        {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception
    {
        // Handle a bad request.
        if (! req.decoderResult().isSuccess())
        {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        // Health check end point
        String path = req.uri();
        if (path != null && path.startsWith(HEALTH_CHECK))
        {
            // handle this health request
            DefaultFullHttpResponse response = this.handleHealthCheck(req);
            // send the response
            ctx.writeAndFlush(response)
                .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        // authenticate the client
        this.authenticated = this.authenticateAgent(req);
        if (this.authenticated)
        {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
            this.handshaker = wsFactory.newHandshaker(req);
            if (this.handshaker == null)
            {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            }
            else
            {
                this.handshaker.handshake(ctx.channel(), req).addListener((future) -> {
                    if (future.isDone() && future.isSuccess()) {
                        // Allow WebSocket traffic to flow
                        this.handshaked = true;
                        // fire connect event
                        logger.info("Accepted Agent connection from " + this.remoteAddress + " agent id " + this.agentId + "/" + this.agentHostName + " for site " + this.siteId);
                        this.server.fireAgentConnect(this);
                    }
                });
            }
        }
        else
        {
            // bad client certificate, terminate the connection
            logger.warn("Failed to authenticate agent connection from " + this.remoteAddress + " possible agent id " + this.agentId);
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
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
        response.headers().set(HttpHeaderNames.SERVER, SERVER_NAME);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, TEXT_PLAIN);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
    
    private boolean authenticateAgent(FullHttpRequest req) throws Exception
    {
        try
        {
            // Extract metadata
            this.agentUserAgent = Util.coalesce(req.headers().get(HttpHeaderNames.USER_AGENT), "Unknown");
            this.agentId = UUID.fromString(requireNonEmpty(req.headers().get(AgentHTTPHeaderNames.AGENT_ID), "agent id"));
            this.agentHostName = requireNonEmpty(req.headers().get(AgentHTTPHeaderNames.HOST_NAME), "agent host name");
            this.agentTemplateName = req.headers().get(AgentHTTPHeaderNames.TEMPLATE_NAME);
            this.authenticationTimestamp = Long.parseLong(requireNonEmpty(req.headers().get(AgentHTTPHeaderNames.TIMESTAMP), "authentication timestamp"));
            this.agentKeyId = UUID.fromString(requireNonEmpty(req.headers().get(AgentHTTPHeaderNames.KEY_ID), "key id"));
            // We can extract the site id from the key id
            this.siteId = SiteMO.getSiteId(this.agentKeyId);
            String authSig = requireNonEmpty(req.headers().get(HttpHeaderNames.AUTHORIZATION), "authorization");
            // Fetch the agent key
            AgentAuthenticationKey key = this.server.getAgentKeyResolver().resolveKey(this.agentKeyId);
            if (key == null) throw new SecurityException("Failed to resolve agent key " + this.agentKeyId);
            // Validate the timestamp
            if (Math.abs((System.currentTimeMillis() / 1000) - this.authenticationTimestamp) > AUTHENTICATION_GRACE_SECONDS)
                throw new SecurityException("Authentication timestamp is not within the grace period.");
            // Validate the signature
            return key.checkBase64(this.authenticationTimestamp, this.agentId, this.agentHostName, this.agentTemplateName, authSig);
        }
        catch (Exception e)
        {
            logger.error("Failed to authenticate agent: " + this.agentId, e);
        }
        return false;
    }
    
    private String requireNonEmpty(String value, String name)
    {
        if (Util.isEmpty(value))
            throw new IllegalArgumentException("The " + name + " must be provided");
        return value;
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
            this.processAgentMessage(ctx, request);
        }
        catch (Exception e)
        {
            logger.error("Failed to decode request", e);
            ctx.close();
        }
    }
    
    private void processAgentMessage(final ChannelHandlerContext ctx, final AgentMessage request) throws Exception
    {
        if (request instanceof AgentPing)
        {
            if (logger.isTraceEnabled()) logger.trace("Got ping from agent");
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
                if (logger.isInfoEnabled()) logger.trace("Got pong from agent");
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
        if (res.status().code() != 200)
        {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (! HttpUtil.isKeepAlive(req) || res.status().code() != 200)
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
        return "wss://" + req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
    }
    
    // Talk to an agent;
    
    public void sendMessageToAgent(AgentMessage message, Consumer<AgentMessage> onResponse)
    {
        // ensure the message has an id
        if (message.getId() == null) message.setId(UUID.randomUUID().toString());
        // stash the message
        this.pendingRequests.put(message.getId(), onResponse);
        // send the message
        this.channel.writeAndFlush(new TextWebSocketFrame(this.transcoder.encodeAsString(message)));
    }
    
    // Helpers
    
    public void sendOnePingAndOnePingOnly(Consumer<Long> onPong)
    {
        this.sendMessageToAgent(new AgentPing(UUID.randomUUID().toString(), System.currentTimeMillis()), (message) -> onPong.accept(System.currentTimeMillis() - ((AgentPong) message).getTimestamp()) );
    }
    
    public void checkAgent(Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckAgent(), onResponse);
    }
    
    public void checkCPU(Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckCPU(), onResponse);
    }
    
    public void checkDisk(Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckDisk(), onResponse);
    }
    
    public void checkDiskIO(List<String> devices, Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckDiskIO(devices), onResponse);
    }
    
    public void checkMem(Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckMem(), onResponse);
    }
    
    public void checkMetrics(String metricNameFilter, boolean stripSourceFromMericName, Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckMetrics(metricNameFilter, stripSourceFromMericName), onResponse);
    }
    
    public void execNagiosCheck(String name, String commandLine, Consumer<AgentMessage> onResponse)
    {
        ExecCheck check = new ExecCheck();
        check.setName(name);
        check.setEngine("nagios");
        check.getParameters().add(new Parameter("command_line", commandLine));
        this.sendMessageToAgent(check, onResponse);
    }
    
    public void execCheck(String engine, String executor, String name, List<Parameter> parameters, Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new ExecCheck(engine, executor, name, parameters), onResponse);
    }
    
    public void checkNetCon(boolean client, boolean server, boolean tcp, boolean udp, boolean unix, boolean raw, int localPort, int remotePort, String localAddress, String remoteAddress, Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckNetCon(client, server, tcp, udp, unix, raw, localPort, remotePort, localAddress, remoteAddress), onResponse);
    }
    
    public void checkNetIO(List<String> interfaces, Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckNetIO(interfaces), onResponse);
    }
    
    public void checkOS(Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckOS(), onResponse);
    }
    
    public void checkProcess(boolean listProcesses, String command, boolean flattenCommand, List<String> arguments, boolean regex, List<String> state, String user, String group, String title, Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckProcess(listProcesses, command, flattenCommand, arguments, regex, state, user, group, title), onResponse);
    }
    
    public void checkUptime(Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckUptime(), onResponse);
    }
    
    public void checkWho(Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new CheckWho(), onResponse);
    }
    
    public void shell(String commandLine, Map<String, String> environment, Consumer<AgentMessage> onResponse)
    {
        this.sendMessageToAgent(new ShellCheck(commandLine, environment), onResponse);
    }
    
    public void shell(String commandLine, Consumer<AgentMessage> onResponse)
    {
        shell(commandLine, null, onResponse);
    }
}
package com.intrbiz.bergamot.agent.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;
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
import com.intrbiz.bergamot.model.message.agent.ping.AgentPing;
import com.intrbiz.bergamot.model.message.agent.ping.AgentPong;
import com.intrbiz.bergamot.proxy.model.ClientHeader;
import com.intrbiz.bergamot.proxy.processor.MessageProcessor;

import io.netty.channel.Channel;

public class BergamotAgentHandler extends MessageProcessor implements BergamotAgent
{
    private static final Logger logger = Logger.getLogger(BergamotAgentHandler.class);
    
    private final UUID nonce = UUID.randomUUID();
    
    private final Consumer<BergamotAgentHandler> pingHandler;
    
    private final Consumer<BergamotAgentHandler> startHandler;
    
    private final Consumer<BergamotAgentHandler> stopHandler;
    
    private final ConcurrentMap<UUID, Consumer<Message>> pendingRequests = new ConcurrentHashMap<>();
    
    public BergamotAgentHandler(ClientHeader client, Channel channel, Consumer<BergamotAgentHandler> pingHandler, Consumer<BergamotAgentHandler> startHandler, Consumer<BergamotAgentHandler> stopHandler)
    {
        super(client.getId(), client, channel);
        this.pingHandler = pingHandler;
        this.startHandler = startHandler;
        this.stopHandler = stopHandler;
    }

    public UUID getNonce()
    {
        return this.nonce;
    }
    
    public UUID getAgentId()
    {
        return this.id;
    }
    
    public UUID getAgentKeyId()
    {
        return this.client.getKeyId();
    }
    
    public UUID getSiteId()
    {
        return this.client.getAllowedSiteId();
    }

    public String getAgentUserAgent()
    {
        return this.client.getUserAgent();
    }

    public String getAgentHostName()
    {
        return this.client.getHostName();
    }

    public String getAgentHostSummary()
    {
        return Util.coalesceEmpty(this.client.getInfo(), this.client.getHostSummary());
    }

    public String getAgentAddress()
    {
        return ((InetSocketAddress) this.channel.remoteAddress()).getAddress().getHostAddress();
    }

    public String getAgentTemplateName()
    {
        return this.client.getTemplateName();
    }

    public SocketAddress getRemoteAddress()
    {
        return this.channel.remoteAddress();
    }
    
    @Override
    public void start()
    {
        if (this.startHandler != null)
        {
            this.startHandler.accept(this);
        }
    }

    @Override
    public void stop()
    {
        if (this.stopHandler != null)
        {
            this.stopHandler.accept(this);
        }
    }

    @Override
    public void processMessage(Message request)
    {
        if (request instanceof AgentPing)
        {
            if (this.pingHandler != null)
            {
                this.pingHandler.accept(this);
            }
            channel.write(new AgentPong((AgentPing) request));
        }
        else
        {
            if (request.getReplyTo() != null)
            {
                // do we have a callback to invoke
                Consumer<Message> callback = this.pendingRequests.remove(request.getReplyTo());
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
    
    // Talk to an agent
    
    public void sendMessageToAgent(Message message, Consumer<Message> onResponse)
    {
        // store the message consumer
        if (message.getId() != null && onResponse != null)
        {
            this.pendingRequests.put(message.getId(), onResponse);
        }
        // send the message
        this.channel.writeAndFlush(message);
    }
    
    public void sendOnePingAndOnePingOnly(Consumer<Long> onPong)
    {
        this.sendMessageToAgent(new AgentPing(System.currentTimeMillis()), (message) -> onPong.accept(System.currentTimeMillis() - ((AgentPong) message).getTimestamp()) );
    }
    
    public void checkAgent(Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckAgent(), onResponse);
    }
    
    public void checkCPU(Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckCPU(), onResponse);
    }
    
    public void checkDisk(Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckDisk(), onResponse);
    }
    
    public void checkDiskIO(List<String> devices, Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckDiskIO(devices), onResponse);
    }
    
    public void checkMem(Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckMem(), onResponse);
    }
    
    public void checkMetrics(String metricNameFilter, boolean stripSourceFromMericName, Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckMetrics(metricNameFilter, stripSourceFromMericName), onResponse);
    }
    
    public void execNagiosCheck(String name, String commandLine, Consumer<Message> onResponse)
    {
        ExecCheck check = new ExecCheck();
        check.setName(name);
        check.setEngine("nagios");
        check.getParameters().add(new ParameterMO("command_line", commandLine));
        this.sendMessageToAgent(check, onResponse);
    }
    
    public void execCheck(String engine, String executor, String name, List<ParameterMO> parameters, Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new ExecCheck(engine, executor, name, parameters), onResponse);
    }
    
    public void checkNetCon(boolean client, boolean server, boolean tcp, boolean udp, boolean unix, boolean raw, int localPort, int remotePort, String localAddress, String remoteAddress, Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckNetCon(client, server, tcp, udp, unix, raw, localPort, remotePort, localAddress, remoteAddress), onResponse);
    }
    
    public void checkNetIO(List<String> interfaces, Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckNetIO(interfaces), onResponse);
    }
    
    public void checkOS(Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckOS(), onResponse);
    }
    
    public void checkProcess(boolean listProcesses, String command, boolean flattenCommand, List<String> arguments, boolean regex, List<String> state, String user, String group, String title, Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckProcess(listProcesses, command, flattenCommand, arguments, regex, state, user, group, title), onResponse);
    }
    
    public void checkUptime(Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckUptime(), onResponse);
    }
    
    public void checkWho(Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new CheckWho(), onResponse);
    }
    
    public void shell(String commandLine, Map<String, String> environment, Consumer<Message> onResponse)
    {
        this.sendMessageToAgent(new ShellCheck(commandLine, environment), onResponse);
    }
    
    public void shell(String commandLine, Consumer<Message> onResponse)
    {
        shell(commandLine, null, onResponse);
    }
}
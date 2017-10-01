package com.intrbiz.bergamot.worker.engine.agent.script;

import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.crypto.util.CertInfo;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.hello.AgentHello;
import com.intrbiz.bergamot.model.message.agent.util.Parameter;

/**
 * Helper wrapper around BergamotAgentServerHandler
 */
public class BergamotAgentScriptWrapper
{
    private final BergamotAgentServerHandler agent;
    
    public BergamotAgentScriptWrapper(BergamotAgentServerHandler agent)
    {
        super();
        this.agent = agent;
    }

    public UUID getAgentId()
    {
        return agent.getAgentId();
    }

    public UUID getSiteId()
    {
        return agent.getSiteId();
    }

    public CertInfo getAgentCertificateInfo()
    {
        return agent.getAgentCertificateInfo();
    }

    public String getAgentName()
    {
        return agent.getAgentName();
    }

    public CertInfo getSiteCertificateInfo()
    {
        return agent.getSiteCertificateInfo();
    }

    public AgentHello getHello()
    {
        return agent.getHello();
    }

    public SocketAddress getRemoteAddress()
    {
        return agent.getRemoteAddress();
    }

    public void sendMessageToAgent(AgentMessage message, Consumer<AgentMessage> onResponse)
    {
        agent.sendMessageToAgent(message, onResponse);
    }

    public void sendOnePingAndOnePingOnly(Consumer<Long> onPong)
    {
        agent.sendOnePingAndOnePingOnly(onPong);
    }

    public void checkAgent(Consumer<AgentMessage> onResponse)
    {
        agent.checkAgent(onResponse);
    }

    public void checkCPU(Consumer<AgentMessage> onResponse)
    {
        agent.checkCPU(onResponse);
    }

    public void checkDisk(Consumer<AgentMessage> onResponse)
    {
        agent.checkDisk(onResponse);
    }

    public void checkDiskIO(List<String> devices, Consumer<AgentMessage> onResponse)
    {
        agent.checkDiskIO(devices, onResponse);
    }

    public void checkMem(Consumer<AgentMessage> onResponse)
    {
        agent.checkMem(onResponse);
    }

    public void checkMetrics(String metricNameFilter, boolean stripSourceFromMericName, Consumer<AgentMessage> onResponse)
    {
        agent.checkMetrics(metricNameFilter, stripSourceFromMericName, onResponse);
    }

    public void execNagiosCheck(String name, String commandLine, Consumer<AgentMessage> onResponse)
    {
        agent.execNagiosCheck(name, commandLine, onResponse);
    }

    public void execCheck(String engine, String executor, String name, List<Parameter> parameters, Consumer<AgentMessage> onResponse)
    {
        agent.execCheck(engine, executor, name, parameters, onResponse);
    }

    public void checkNetCon(boolean client, boolean server, boolean tcp, boolean udp, boolean unix, boolean raw, int localPort, int remotePort, String localAddress, String remoteAddress, Consumer<AgentMessage> onResponse)
    {
        agent.checkNetCon(client, server, tcp, udp, unix, raw, localPort, remotePort, localAddress, remoteAddress, onResponse);
    }

    public void checkNetIO(List<String> interfaces, Consumer<AgentMessage> onResponse)
    {
        agent.checkNetIO(interfaces, onResponse);
    }

    public void checkOS(Consumer<AgentMessage> onResponse)
    {
        agent.checkOS(onResponse);
    }

    public void checkProcess(boolean listProcesses, String command, boolean flattenCommand, List<String> arguments, boolean regex, List<String> state, String user, String group, String title, Consumer<AgentMessage> onResponse)
    {
        agent.checkProcess(listProcesses, command, flattenCommand, arguments, regex, state, user, group, title, onResponse);
    }

    public void checkUptime(Consumer<AgentMessage> onResponse)
    {
        agent.checkUptime(onResponse);
    }

    public void checkWho(Consumer<AgentMessage> onResponse)
    {
        agent.checkWho(onResponse);
    }
}

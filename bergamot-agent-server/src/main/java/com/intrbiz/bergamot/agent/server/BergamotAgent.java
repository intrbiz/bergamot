package com.intrbiz.bergamot.agent.server;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;

public interface BergamotAgent
{
    UUID getNonce();
    
    UUID getAgentId();
    
    UUID getAgentKeyId();
    
    UUID getSiteId();

    String getAgentUserAgent();

    String getAgentHostName();

    String getAgentHostSummary();

    String getAgentAddress();

    String getAgentTemplateName();

    SocketAddress getRemoteAddress();
    
    void sendMessageToAgent(Message message, Consumer<Message> onResponse);
    
    void sendOnePingAndOnePingOnly(Consumer<Long> onPong);
    
    void checkAgent(Consumer<Message> onResponse);
    
    void checkCPU(Consumer<Message> onResponse);
    
    void checkDisk(Consumer<Message> onResponse);
    
    void checkDiskIO(List<String> devices, Consumer<Message> onResponse);
    
    void checkMem(Consumer<Message> onResponse);
    
    void checkMetrics(String metricNameFilter, boolean stripSourceFromMericName, Consumer<Message> onResponse);
    
    void execNagiosCheck(String name, String commandLine, Consumer<Message> onResponse);
    
    void execCheck(String engine, String executor, String name, List<ParameterMO> parameters, Consumer<Message> onResponse);
    
    void checkNetCon(boolean client, boolean server, boolean tcp, boolean udp, boolean unix, boolean raw, int localPort, int remotePort, String localAddress, String remoteAddress, Consumer<Message> onResponse);
    
    void checkNetIO(List<String> interfaces, Consumer<Message> onResponse);
    
    void checkOS(Consumer<Message> onResponse);
    
    void checkProcess(boolean listProcesses, String command, boolean flattenCommand, List<String> arguments, boolean regex, List<String> state, String user, String group, String title, Consumer<Message> onResponse);
    
    void checkUptime(Consumer<Message> onResponse);
    
    void checkWho(Consumer<Message> onResponse);
    
    void shell(String commandLine, Map<String, String> environment, Consumer<Message> onResponse);
    
    void shell(String commandLine, Consumer<Message> onResponse);
}

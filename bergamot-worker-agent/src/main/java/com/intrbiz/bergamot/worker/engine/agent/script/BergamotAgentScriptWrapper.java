package com.intrbiz.bergamot.worker.engine.agent.script;

import static com.intrbiz.bergamot.worker.engine.script.NashornUtil.*;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.intrbiz.bergamot.agent.server.BergamotAgent;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * Helper wrapper around BergamotAgentServerHandler
 */
@SuppressWarnings("restriction")
public class BergamotAgentScriptWrapper
{
    private final BergamotAgent agent;
    
    public BergamotAgentScriptWrapper(BergamotAgent agent)
    {
        super();
        this.agent = agent;
    }

    public UUID getAgentId()
    {
        return this.agent.getAgentId();
    }

    public UUID getSiteId()
    {
        return this.agent.getSiteId();
    }

    public SocketAddress getRemoteAddress()
    {
        return this.agent.getRemoteAddress();
    }

    public void sendMessageToAgent(Message message, Consumer<Message> onResponse)
    {
        this.agent.sendMessageToAgent(message, onResponse);
    }

    public void sendOnePingAndOnePingOnly(Consumer<Long> onPong)
    {
        this.agent.sendOnePingAndOnePingOnly(onPong);
    }

    public void checkAgent(Consumer<Message> onResponse)
    {
        this.agent.checkAgent(onResponse);
    }

    public void checkCPU(Consumer<Message> onResponse)
    {
        this.agent.checkCPU(onResponse);
    }

    public void checkDisk(Consumer<Message> onResponse)
    {
        this.agent.checkDisk(onResponse);
    }

    public void checkDiskIO(List<String> devices, Consumer<Message> onResponse)
    {
        this.agent.checkDiskIO(devices, onResponse);
    }

    public void checkMem(Consumer<Message> onResponse)
    {
        this.agent.checkMem(onResponse);
    }

    public void checkMetrics(String metricNameFilter, boolean stripSourceFromMericName, Consumer<Message> onResponse)
    {
        this.agent.checkMetrics(metricNameFilter, stripSourceFromMericName, onResponse);
    }
    
    public void checkMetrics(ScriptObjectMirror props, Consumer<Message> onResponse)
    {
        this.agent.checkMetrics(mapString(props, "metricNameFilter"), mapBoolean(props, "stripSourceFromMericName", true), onResponse);
    }

    public void execNagiosCheck(String name, String commandLine, Consumer<Message> onResponse)
    {
        this.agent.execNagiosCheck(name, commandLine, onResponse);
    }
    
    public void execNagiosCheck(ScriptObjectMirror props, Consumer<Message> onResponse)
    {
        this.agent.execNagiosCheck(mapString(props, "name"), mapString(props, "commandLine"), onResponse);
    }

    public void execCheck(String engine, String executor, String name, List<ParameterMO> parameters, Consumer<Message> onResponse)
    {
        this.agent.execCheck(engine, executor, name, parameters, onResponse);
    }
    
    public void execCheck(ScriptObjectMirror props, Consumer<Message> onResponse)
    {
        this.agent.execCheck(
                mapString(props, "engine"), 
                mapString(props, "executor"), 
                mapString(props, "name"), 
                mapJSObjectToAgentParameters(props, "parameters", new LinkedList<ParameterMO>()), 
                onResponse
        );
    }

    public void checkNetCon(boolean client, boolean server, boolean tcp, boolean udp, boolean unix, boolean raw, int localPort, int remotePort, String localAddress, String remoteAddress, Consumer<Message> onResponse)
    {
        this.agent.checkNetCon(client, server, tcp, udp, unix, raw, localPort, remotePort, localAddress, remoteAddress, onResponse);
    }
    
    public void checkNetCon(ScriptObjectMirror props, Consumer<Message> onResponse)
    {
        this.agent.checkNetCon(
                mapBoolean(props, "client", false), 
                mapBoolean(props, "server", true), 
                mapBoolean(props, "tcp", true), 
                mapBoolean(props, "udp", true), 
                mapBoolean(props, "unix", false), 
                mapBoolean(props, "raw", false), 
                mapInteger(props, "localPort", 0), 
                mapInteger(props, "remotePort", 0), 
                mapString(props, "localAddress"), 
                mapString(props, "remoteAddress"), 
                onResponse
        );
    }

    public void checkNetIO(List<String> interfaces, Consumer<Message> onResponse)
    {
        this.agent.checkNetIO(interfaces, onResponse);
    }

    public void checkOS(Consumer<Message> onResponse)
    {
        this.agent.checkOS(onResponse);
    }

    public void checkProcess(boolean listProcesses, String command, boolean flattenCommand, List<String> arguments, boolean regex, List<String> state, String user, String group, String title, Consumer<Message> onResponse)
    {
        this.agent.checkProcess(listProcesses, command, flattenCommand, arguments, regex, state, user, group, title, onResponse);
    }
    
    public void checkProcess(ScriptObjectMirror props, Consumer<Message> onResponse)
    {
        this.agent.checkProcess(
                mapBoolean(props, "listProcesses", true), 
                mapString(props, "command"), 
                mapBoolean(props, "flattenCommand", false), 
                mapJsArrayOfStrings(props, "arguments", new LinkedList<String>()), 
                mapBoolean(props, "regex", false), 
                mapJsArrayOfStrings(props, "state", new LinkedList<String>()), 
                mapString(props, "user"), 
                mapString(props, "group"), 
                mapString(props, "title"), 
                onResponse
        );
    }

    public void checkUptime(Consumer<Message> onResponse)
    {
        this.agent.checkUptime(onResponse);
    }

    public void checkWho(Consumer<Message> onResponse)
    {
        this.agent.checkWho(onResponse);
    }
    
    public void shell(String commandLine, Map<String, String> environment, Consumer<Message> onResponse)
    {
        this.agent.shell(commandLine, environment, onResponse);
    }
    
    public void shell(String commandLine, Consumer<Message> onResponse)
    {
        this.agent.shell(commandLine, onResponse);
    }
    
    public static List<ParameterMO> mapJSObjectToAgentParameters(ScriptObjectMirror object, String property, List<ParameterMO> defaultValue)
    {
        if (object != null)
        {
            Object prop = object.get(property);
            if (prop instanceof ScriptObjectMirror)
            {
                List<ParameterMO> ret = new LinkedList<ParameterMO>();
                ScriptObjectMirror params = (ScriptObjectMirror) prop;
                for (String key : params.getOwnKeys(false))
                {
                    Object value = params.get(key);
                    ret.add(new ParameterMO(key, value == null ? null : value.toString()));
                }
                return ret;
            }
        }
        return defaultValue;
    }
}

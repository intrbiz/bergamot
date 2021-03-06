package com.intrbiz.bergamot.agent.handler;

import com.intrbiz.bergamot.agent.BergamotAgent;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;

public class AgentInfoHandler extends AbstractAgentHandler
{
    public AgentInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckAgent.class
        };
    }

    @Override
    public Message handle(Message request)
    {
        try
        {
            AgentStat stat = new AgentStat(request);
            // agent version
            stat.setAgentVendor(BergamotAgent.AGENT_VENDOR);
            stat.setAgentProduct(BergamotAgent.AGENT_PRODUCT);
            stat.setAgentVersion(BergamotAgent.AGENT_VERSION);
            // runtime info
            stat.setRuntime("Java");
            stat.setRuntimeVendor(System.getProperty("java.vendor"));
            stat.setRuntimeVersion(System.getProperty("java.version"));
            // user
            stat.setUserName(System.getProperty("user.name"));
            // os
            stat.setOsName(System.getProperty("os.name"));
            stat.setOsVersion(System.getProperty("os.version"));
            stat.setOsArch(System.getProperty("os.arch"));
            // memory stats
            Runtime runtime = Runtime.getRuntime();
            stat.setProcessors(runtime.availableProcessors());
            stat.setFreeMemory(runtime.freeMemory());
            stat.setTotalMemory(runtime.totalMemory());
            stat.setMaxMemory(runtime.maxMemory());
            return stat;
        }
        catch (Exception e)
        {
            return new GeneralError(e.getMessage());
        }
    }
}

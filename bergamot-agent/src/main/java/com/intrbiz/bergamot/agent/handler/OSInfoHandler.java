package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.OperatingSystem;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckOS;
import com.intrbiz.bergamot.model.message.agent.stat.OSStat;

public class OSInfoHandler extends AbstractAgentHandler
{

    public OSInfoHandler()
    {
        super();
    }

    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] { CheckOS.class };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        OSStat stat = new OSStat(request);
        OperatingSystem sys = OperatingSystem.getInstance();
        stat.setArch(sys.getArch());
        stat.setDescription(sys.getDescription());
        stat.setMachine(sys.getMachine());
        stat.setName(sys.getName());
        stat.setPatchLevel(sys.getPatchLevel());
        stat.setVendor(sys.getVendor());
        stat.setVendorCodeName(sys.getVendorCodeName());
        stat.setVendorName(sys.getVendorName());
        stat.setVendorVersion(sys.getVendorVersion());
        stat.setVersion(sys.getVersion());
        return stat;
    }
}

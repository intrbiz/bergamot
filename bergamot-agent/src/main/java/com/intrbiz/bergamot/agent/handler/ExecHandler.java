package com.intrbiz.bergamot.agent.handler;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.stat.ExecStat;
import com.intrbiz.bergamot.nagios.NagiosPluginExecutor;
import com.intrbiz.bergamot.nagios.model.NagiosResult;

public class ExecHandler implements AgentHandler
{
    private Logger logger = Logger.getLogger(ExecHandler.class);
    
    private NagiosPluginExecutor executor;
    
    public ExecHandler()
    {
        super();
        this.executor = new NagiosPluginExecutor();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                ExecCheck.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        ExecCheck check = (ExecCheck) request;
        //
        ExecStat stat = new ExecStat(request);
        //
        try
        {
            if ("nagios".equalsIgnoreCase(check.getEngine()))
            {
                // validate the command line
                String commandLine = check.getParameter("command_line");
                if (Util.isEmpty(commandLine)) throw new RuntimeException("The command_line must be defined!");
                NagiosResult response = this.executor.execute(commandLine);
                stat.setOk(response.toOk());
                stat.setStatus(response.toStatus());
                stat.setOutput(response.getOutput());
                stat.setRuntime(response.getRuntime());
            }
            else
            {
                stat.error("Cannot execute check, unknown engine!");
            }
        }
        catch (IOException | InterruptedException e)
        {
            logger.error("Failed to execute nagios check command", e);
            stat.error(e);
        }
        //
        return stat;
    }
}

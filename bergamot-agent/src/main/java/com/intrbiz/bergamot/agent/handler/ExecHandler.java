package com.intrbiz.bergamot.agent.handler;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.util.AgentUtil;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.bergamot.model.message.agent.check.ExecCheck;
import com.intrbiz.bergamot.model.message.agent.stat.ExecStat;
import com.intrbiz.bergamot.nagios.NagiosPluginExecutor;
import com.intrbiz.bergamot.nagios.model.NagiosPerfData;
import com.intrbiz.bergamot.nagios.model.NagiosResult;
import com.intrbiz.gerald.polyakov.Reading;

public class ExecHandler extends AbstractAgentHandler
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
    public Message handle(Message request)
    {
        ExecCheck check = (ExecCheck) request;
        //
        ExecStat stat = new ExecStat(request);
        //
        if (Boolean.getBoolean("bergamot.agent.no-exec") || "true".equals(System.getenv("BERGAMOT_AGENT_NO_EXEC")))
        {
            logger.error("Cannot execute check, administratively disabled!");
            stat.error("Cannot execute check, administratively disabled!");
        }
        else
        {
            try
            {
                if ("nagios".equalsIgnoreCase(check.getEngine()))
                {
                    // validate the command line
                    String commandLine = getParameter(check.getParameters(), "command_line");
                    if (AgentUtil.isEmpty(commandLine)) throw new RuntimeException("The command_line must be defined!");
                    NagiosResult response = this.executor.execute(commandLine);
                    stat.setOk(response.toOk());
                    stat.setStatus(response.toStatus());
                    stat.setOutput(response.getOutput());
                    stat.setRuntime(response.getRuntime());
                    // readings
                    stat.setCaptured(System.currentTimeMillis());
                    for (NagiosPerfData perf : response.getPerfData())
                    {
                        Reading reading = perf.toReading();
                        if (reading != null) stat.getReadings().add(reading);
                    }
                }
                else
                {
                    stat.error("Cannot execute check, unknown engine!");
                }
            }
            catch (IOException e)
            {
                logger.error("Failed to execute nagios check command", e);
                stat.error(e);
            }
            catch (InterruptedException e)
            {
                logger.error("Failed to execute nagios check command", e);
                stat.error(e);
            }
        }
        //
        return stat;
    }
    
    private static String getParameter(List<ParameterMO> parameters, String name)
    {
        for (ParameterMO parameter : parameters)
        {
            if (name.equals(parameter.getName()))
                return parameter.getValue();
        }
        return null;
    }
}

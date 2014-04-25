package com.intrbiz.bergamot.worker.engine.nagios.nrpe;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.macro.MacroFrame;
import com.intrbiz.bergamot.compat.macro.MacroProcessor;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.task.ExecuteCheck;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.bergamot.nrpe.NRPEClient;
import com.intrbiz.bergamot.nrpe.model.NRPEResponse;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.nagios.util.NagiosPluginParser;

/**
 * Execute NRPE checks from pure Java
 */
public class NRPEExecutor extends AbstractCheckExecutor<NRPEEngine>
{
    private Logger logger = Logger.getLogger(NRPEExecutor.class);
    
    public NRPEExecutor()
    {
        super();
    }
    
    /**
     * Only execute Checks where the engine == "nrpe"
     */
    @Override
    public boolean accept(Task task)
    {
        return super.accept(task) && (task instanceof ExecuteCheck) && NRPEEngine.NAME.equals(((ExecuteCheck) task).getEngine());
    }
    
    @Override
    public Result run(ExecuteCheck executeCheck)
    {
        logger.info("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        Result result = executeCheck.createResult();
        try
        {
            // the host
            String host = executeCheck.getParameter("host");
            if (Util.isEmpty(host)) throw new RuntimeException("The 'host' parameter must be provided.");
            // the command
            String command = executeCheck.getParameter("command");
            if (Util.isEmpty(host)) throw new RuntimeException("The 'command' parameter must be provided.");
            // currently we apply the nagios macros here, this will change
            MacroFrame checkFrame = MacroFrame.fromParameters(executeCheck.getParameters());
            host = MacroProcessor.applyMacros(host, checkFrame);
            command = MacroProcessor.applyMacros(command, checkFrame);
            // TODO arguments support
            // open an NRPE client and execute the command
            try (NRPEClient client = new NRPEClient(host))
            {
                NRPEResponse response = client.command(command);
                // parse the response
                NagiosPluginParser.parseNagiosExitCode(response.getResponseCode(), result);
                // parse the output
                NagiosPluginParser.parseNagiosOutput(response.getOutput(), result);
                // runtime
                result.setRuntime(response.getRuntime());
            }
        }
        catch (Exception e)
        {
            // TODO handle NRPE down
            logger.error("Failed to execute NRPE check", e);
            result.setOk(false);
            result.setStatus(Status.INTERNAL);
            result.setOutput(e.getMessage());
            result.setRuntime(0);
        }
        return result;
    }
}

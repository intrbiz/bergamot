package com.intrbiz.bergamot.worker.engine.nrpe;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.nagios.util.NagiosPluginParser;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.queue.Producer;

/**
 * Execute NRPE checks from pure Java
 */
public class NRPEExecutor extends AbstractExecutor<NRPEEngine>
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
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && (task instanceof ExecuteCheck) && NRPEEngine.NAME.equals(((ExecuteCheck) task).getEngine());
    }
    
    @Override
    public void execute(ExecuteCheck executeCheck, Producer<Result> resultSubmitter)
    {
        logger.info("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        final Result result = executeCheck.createResult();
        try
        {
            // the host
            String host = executeCheck.getParameter("host");
            if (Util.isEmpty(host)) throw new RuntimeException("The 'host' parameter must be provided.");
            // the command
            String command = executeCheck.getParameter("command");
            if (Util.isEmpty(command)) throw new RuntimeException("The 'command' parameter must be provided.");
            // submit the command to the poller
            // TODO timeouts
            this.getEngine().getPoller().command(
                    host, 
                    5666, 
                    5, 
                    60, 
                    null, 
                    (response, context) -> {
                        try
                        {
                            // parse the response
                            NagiosPluginParser.parseNagiosExitCode(response.getResponseCode(), result);
                            // parse the output
                            NagiosPluginParser.parseNagiosOutput(response.getOutput(), result);
                            // runtime
                            result.setRuntime(response.getRuntime());
                            // publish
                            logger.debug("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
                            resultSubmitter.publish(result);
                        }
                        catch (IOException exception)
                        {
                            logger.error("Failed to parse NRPE output", exception);
                            result.setOk(false);
                            result.setStatus("ERROR");
                            result.setOutput("Failed to parse NRPE output: " + exception.getMessage());
                            result.setRuntime(0);
                            // publish
                            logger.debug("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
                            resultSubmitter.publish(result);    
                        }
                    }, 
                    (exception, context) -> {
                        logger.error("Failed to execute NRPE check", exception);
                        result.setOk(false);
                        result.setStatus("ERROR");
                        result.setOutput(exception.getMessage());
                        result.setRuntime(0);
                        // publish
                        logger.debug("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
                        resultSubmitter.publish(result);
                    },
                    command
            );
        }
        catch (Exception e)
        {
            logger.error("Failed to execute NRPE check", e);
            result.setOk(false);
            result.setStatus("ERROR");
            result.setOutput(e.getMessage());
            result.setRuntime(0);
            // publish
            logger.debug("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
            resultSubmitter.publish(result);
        }        
    }
}

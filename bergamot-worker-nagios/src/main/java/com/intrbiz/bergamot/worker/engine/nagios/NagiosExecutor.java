package com.intrbiz.bergamot.worker.engine.nagios;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.nagios.util.NagiosPluginParser;
import com.intrbiz.bergamot.util.CommandTokeniser;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Execute Nagios Plugins (CHecks)
 * 
 * This CheckExecutor executes Nagios plugins (checks) by 
 * forking the check, just like Nagios would.
 * 
 * The exit code is read to determine the result status, 
 * and stdOut/stdErr are read to determine the check output.
 * 
 * Performance data is captured and stored as a parameter of 
 * the result.
 * 
 * Currently only the first line of the check output is read.
 * 
 * To avoid polluting the core of Bergamot with Nagios specific 
 * functionality, macro processing is pushed out to the edge.
 * As such the only component which needs to known about Nagios 
 * macros is this Nagios specific CheckExecutor implementation.
 * 
 */
public class NagiosExecutor extends AbstractExecutor<NagiosEngine>
{
    private Logger logger = Logger.getLogger(NagiosExecutor.class);

    protected File workingDirectory;

    protected Map<String, String> environmentVariables = new HashMap<String, String>();

    public NagiosExecutor()
    {
        super();
        // set the working directory
        this.workingDirectory = new File(System.getProperty("bergamot.worker.dir", System.getProperty("user.dir", ".")));
        // setup the environment variables
    }

    /**
     * Only execute Checks where the engine == "nagios"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return super.accept(task) && (task instanceof ExecuteCheck) && "nagios".equals(((ExecuteCheck) task).getEngine());
    }

    @Override
    public Result execute(ExecuteCheck executeCheck)
    {
        logger.info("Executing check : " + executeCheck.getEngine() + "::" + executeCheck.getName() + " for " + executeCheck.getCheckType() + " " + executeCheck.getCheckId());
        Result result = executeCheck.createResult();
        try
        {
            // apply macros to build the command line
            String commandLine = this.buildCommandLine(executeCheck);
            // build the process
            List<String> command = CommandTokeniser.tokeniseCommandLine(commandLine);
            logger.trace("Tokenised command line: '" + commandLine + "' => " + command);
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(this.workingDirectory);
            builder.environment().putAll(this.environmentVariables);
            builder.redirectErrorStream(true);
            // launch the process
            // TODO watchdog
            long start = System.nanoTime();
            Process process = null;
            try
            {
                process  = builder.start();
                InputStream stdOut = process.getInputStream();
                int exitCode = process.waitFor();
                long end = System.nanoTime();
                // process the result
                NagiosPluginParser.parseNagiosExitCode(exitCode, result);
                NagiosPluginParser.parseNagiosOutput(stdOut, result);
                result.setRuntime((((double) (end - start)) / 1_000_000D));
                // log
                logger.info("Check output: " + result.isOk() + " " + result.getStatus());
                logger.debug("Check took: " + (((double) (end - start)) / 1_000_000D) + " ms");
            }
            finally
            {
                if (process != null) process.destroy();
            }
        }
        catch (IOException | InterruptedException e)
        {
            logger.error("Failed to execute nagios check command", e);
            result.setOk(false);
            result.setStatus("ERROR");
            result.setOutput(e.getMessage());
            result.setRuntime(0);
        }
        return result;
    }
    
    /**
     * The check parameters contain the command line to 
     * execute and the variable required to build the command line.
     * 
     * As such we need to process the Nagios macros contained in 
     * the command line.
     * 
     */
    protected String buildCommandLine(ExecuteCheck executeCheck)
    {
        // at minimum we must have the command line and check command
        String commandLine = executeCheck.getParameter("command_line");
        if (Util.isEmpty(commandLine)) throw new RuntimeException("The command_line must be defined!");
        // the master will have computed the command line for us
        return commandLine;
    }

}

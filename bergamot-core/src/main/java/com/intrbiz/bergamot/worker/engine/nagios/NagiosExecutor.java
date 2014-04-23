package com.intrbiz.bergamot.worker.engine.nagios;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.macro.MacroFrame;
import com.intrbiz.bergamot.compat.macro.MacroProcessor;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.task.ExecuteCheck;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.util.CommandTokeniser;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;

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
public class NagiosExecutor extends AbstractCheckExecutor<NagiosEngine>
{
    public static final int NAGIOS_OK = 0;

    public static final int NAGIOS_WARNING = 1;

    public static final int NAGIOS_CRITICAL = 2;

    public static final int NAGIOS_UNKNOWN = 3;

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
    public boolean accept(Task task)
    {
        return super.accept(task) && (task instanceof ExecuteCheck) && "nagios".equals(((ExecuteCheck) task).getEngine());
    }

    @Override
    public Result run(ExecuteCheck executeCheck)
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
                this.parseNagiosExitCode(exitCode, result);
                this.parseNagiosOutput(stdOut, result);
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
            result.setStatus(Status.INTERNAL);
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
        // construct the macro frame from the check parameters
        MacroFrame checkFrame = new MacroFrame();
        for (Parameter parameter : executeCheck.getParameters())
        {
            checkFrame.put(parameter.getName(), parameter.getValue());
        }
        // apply the macros to the command line to form the final command line
        logger.trace("Applying macros to " + commandLine);
        return MacroProcessor.applyMacros(commandLine, checkFrame);
    }

    /**
     * Convert a Nagios exit code to a Bergamot result
     */
    protected void parseNagiosExitCode(int code, Result result)
    {
        switch (code)
        {
            case NAGIOS_OK:
                result.setOk(true);
                result.setStatus(Status.OK);
                break;
            case NAGIOS_WARNING:
                result.setOk(false);
                result.setStatus(Status.WARNING);
                break;
            case NAGIOS_CRITICAL:
                result.setOk(false);
                result.setStatus(Status.CRITICAL);
                break;
            case NAGIOS_UNKNOWN:
            default:
                result.setOk(false);
                result.setStatus(Status.UNKNOWN);
                break;
        }
    }

    /**
     * Extract the check output
     */
    protected void parseNagiosOutput(InputStream stream, Result result) throws IOException
    {
        // currently only look at the first line
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream)))
        {
            String outputLine = br.readLine();
            if (outputLine != null)
            {
                logger.trace("Got output line: " + outputLine);
                int pipe = outputLine.indexOf("|");
                if (pipe > 0 && pipe < outputLine.length())
                {
                    result.setOutput(outputLine.substring(0, pipe).trim());
                    result.addParameter("nagios_perf_data", outputLine.substring(pipe + 1).trim());
                }
                else
                {
                    result.setOutput(outputLine.trim());
                }
            }
            else
            {
                logger.trace("No output returned from plugin");
            }
        }
    }
}

package com.intrbiz.bergamot.nagios;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.nagios.model.NagiosResult;
import com.intrbiz.bergamot.util.CommandTokeniser;

/**
 * Execute a Nagios process
 */
public class NagiosPluginExecutor
{
    private Logger logger = Logger.getLogger(NagiosPluginExecutor.class);

    protected Map<String, String> environmentVariables = new HashMap<String, String>();

    protected File workingDirectory;

    public NagiosPluginExecutor()
    {
        super();
        // set the working directory
        this.workingDirectory = new File(System.getProperty("bergamot.worker.dir", System.getProperty("user.dir", ".")));
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory)
    {
        this.workingDirectory = workingDirectory;
    }

    public void addEnvironmentVariable(String name, String value)
    {
        this.environmentVariables.put(name, value);
    }

    public String getEnvironmentVariable(String name)
    {
        return this.environmentVariables.get(name);
    }

    public Map<String, String> getEnvironmentVariables()
    {
        return this.environmentVariables;
    }

    public NagiosResult execute(String commandLine) throws IOException, InterruptedException
    {
        // build the process
        List<String> command = CommandTokeniser.tokeniseCommandLine(commandLine);
        logger.trace("Tokenised command line: '" + commandLine + "' => " + command);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(this.workingDirectory);
        builder.environment().putAll(this.environmentVariables);
        builder.redirectErrorStream(true);
        // fork the process
        long start = System.nanoTime();
        // TODO watchdog the process
        Process process = null;
        try
        {
            process = builder.start();
            InputStream stdOut = process.getInputStream();
            int exitCode = process.waitFor();
            long end = System.nanoTime();
            double runtime = (((double) (end - start)) / 1000000D);
            // process the output
            NagiosResult result = new NagiosResult();
            result.parseNagiosOutput(stdOut, exitCode, runtime);
            logger.info("Plugin output: " + result.getResponseCode() + " " + result.getOutput());
            logger.debug("Plugin execution took: " + result.getRuntime() + " ms");
            return result;
        }
        finally
        {
            if (process != null) process.destroy();
        }
    }
}

package com.intrbiz.bergamot.agent.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.ShellCheck;
import com.intrbiz.bergamot.model.message.agent.stat.ShellStat;
import com.intrbiz.bergamot.util.CommandTokeniser;

public class ShellHandler extends AbstractAgentHandler
{
    private Logger logger = Logger.getLogger(ShellHandler.class);
    
    protected File workingDirectory;
    
    public ShellHandler()
    {
        super();
        // set the working directory
        this.workingDirectory = new File(System.getProperty("bergamot.worker.dir", System.getProperty("user.dir", ".")));
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                ShellCheck.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        ShellCheck check = (ShellCheck) request;
        ShellStat stat = new ShellStat(request);
        long start = System.currentTimeMillis();
        try
        {
            // tokenise the commandline
            List<String> command = CommandTokeniser.tokeniseCommandLine(check.getCommandLine());
            logger.info("Executing shell command: " + command);
            // build the process
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(this.workingDirectory);
            builder.environment().clear();
            if (check.getEnvironment() != null)
            {
                for (Entry<String, String> var : check.getEnvironment().entrySet())
                {
                    builder.environment().put(var.getKey(), var.getValue());
                }
            }
            builder.redirectErrorStream(true);
            // execute the process
            Process process = null;
            try
            {
                process = builder.start();
                InputStream stdOut = process.getInputStream();
                stat.setExit(process.waitFor());
                stat.setOutput(bufferOutput(stdOut));
            }
            finally
            {
                if (process != null) process.destroy();
            }
        }
        catch (Exception e)
        {
            stat.setExit(-1);
            stat.setOutput(e.getMessage());
        }
        stat.setRuntime(System.currentTimeMillis() - start);
        return stat;
    }
    
    private static String bufferOutput(InputStream inp) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        char[] b = new char[1024];
        int r;
        InputStreamReader rdr = new InputStreamReader(inp);
        try
        {
            while ((r = rdr.read(b)) != -1)
            {
                sb.append(b, 0, r);
            }
        }
        finally
        {
            rdr.close();
        }
        return sb.toString();
    }
}

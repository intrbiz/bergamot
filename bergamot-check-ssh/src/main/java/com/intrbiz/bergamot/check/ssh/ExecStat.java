package com.intrbiz.bergamot.check.ssh;

public class ExecStat
{
    private final int exit;
    
    private final String stdOut;
    
    private final String stdErr;
    
    public ExecStat(int exit, String stdOut, String stdErr)
    {
        this.exit = exit;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public int getExit()
    {
        return exit;
    }

    public String getStdOut()
    {
        return stdOut;
    }

    public String getStdErr()
    {
        return stdErr;
    }
}

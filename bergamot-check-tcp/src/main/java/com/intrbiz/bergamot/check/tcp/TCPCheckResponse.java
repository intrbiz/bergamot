package com.intrbiz.bergamot.check.tcp;


public class TCPCheckResponse
{
    private final long runtime;
    
    public TCPCheckResponse(long runtime)
    {
        this.runtime = runtime;
    }

    public long getRuntime()
    {
        return runtime;
    }

    public String toString()
    {
        return "tcp-check-response { runtime: " + this.runtime + "ms }";
    }
}

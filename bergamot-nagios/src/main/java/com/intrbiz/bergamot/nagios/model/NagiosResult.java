package com.intrbiz.bergamot.nagios.model;

public class NagiosResult
{
    /* Nagios Status Codes */
    
    public static final int OK       = 0;
    
    public static final int WARNING  = 1;
    
    public static final int CRITICAL = 2;
    
    public static final int UNKNOWN  = 3;
    
    private final int responseCode;
    
    private final String output;
    
    private final double runtime;
    
    public NagiosResult(int responseCode, String output, double runtime)
    {
        super();
        this.responseCode = responseCode;
        this.output = output;
        this.runtime = runtime;
    }

    /**
     * The response
     * @return
     */
    public int getResponseCode()
    {
        return responseCode;
    }
    
    public boolean isOk()
    {
        return this.responseCode == OK;
    }
    
    public boolean isWarning()
    {
        return this.responseCode == WARNING;
    }
    
    public boolean isCritical()
    {
        return this.responseCode == CRITICAL;
    }
    
    public boolean isUnknown()
    {
        return this.responseCode == UNKNOWN;
    }

    /**
     * The output
     * @return
     */
    public String getOutput()
    {
        return output;
    }

    /**
     * How long the check took to execute in milliseconds
     * @return
     */
    public double getRuntime()
    {
        return runtime;
    }
    
    public String toString()
    {
        return "Nagios Result: " + this.responseCode + " " + this.output;
    }
}

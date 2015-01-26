package com.intrbiz.bergamot.nagios.model;

public class NagiosResult
{
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

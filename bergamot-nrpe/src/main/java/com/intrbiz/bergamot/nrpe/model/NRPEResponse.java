package com.intrbiz.bergamot.nrpe.model;

/**
 * The response for a NRPE check
 */
public class NRPEResponse
{
    private final int responseCode;
    
    private final String output;
    
    private final double runtime;
    
    public NRPEResponse(int responseCode, String output, double runtime)
    {
        super();
        this.responseCode = responseCode;
        this.output = output;
        this.runtime = runtime;
    }

    /**
     * The response code as returned by NRPE
     * @return
     */
    public int getResponseCode()
    {
        return responseCode;
    }

    /**
     * The output as returned by NRPE
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
        return "NRPE Response: " + this.responseCode + " " + this.output;
    }
}

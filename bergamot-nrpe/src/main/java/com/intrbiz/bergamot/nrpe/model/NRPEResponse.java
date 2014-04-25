package com.intrbiz.bergamot.nrpe.model;

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

    public int getResponseCode()
    {
        return responseCode;
    }

    public String getOutput()
    {
        return output;
    }

    public double getRuntime()
    {
        return runtime;
    }
    
    public String toString()
    {
        return "NRPE Response: " + this.responseCode + " " + this.output;
    }
}

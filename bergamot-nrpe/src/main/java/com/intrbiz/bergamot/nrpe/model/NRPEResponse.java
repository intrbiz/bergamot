package com.intrbiz.bergamot.nrpe.model;

import com.intrbiz.bergamot.nagios.model.NagiosResult;

/**
 * The response for a NRPE check
 */
public class NRPEResponse extends NagiosResult
{
    public NRPEResponse(int responseCode, String output, double runtime)
    {
        super(responseCode, output, runtime);
    }

    public String toString()
    {
        return "NRPE Response: " + this.getResponseCode() + " " + this.getOutput();
    }
}

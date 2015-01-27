package com.intrbiz.bergamot.nrpe.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.intrbiz.bergamot.nagios.model.NagiosResult;

/**
 * The response for a NRPE check
 */
public class NRPEResponse extends NagiosResult
{
    public NRPEResponse()
    {
        super();
    }

    public NRPEResponse(InputStream stream, int exitCode, double runtime) throws IOException
    {
        super(stream, exitCode, runtime);
    }

    public NRPEResponse(Reader stream, int exitCode, double runtime) throws IOException
    {
        super(stream, exitCode, runtime);
    }

    public NRPEResponse(String stream, int exitCode, double runtime)
    {
        super(stream, exitCode, runtime);
    }

    public String toString()
    {
        return "NRPE Response: " + this.getResponseCode() + " (" + this.toStatus() + ") " + this.getOutput();
    }
}

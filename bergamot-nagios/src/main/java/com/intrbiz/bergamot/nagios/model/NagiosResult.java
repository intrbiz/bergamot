package com.intrbiz.bergamot.nagios.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public class NagiosResult
{
    /* Nagios Status Codes */
    
    public static final int OK       = 0;
    
    public static final int WARNING  = 1;
    
    public static final int CRITICAL = 2;
    
    public static final int UNKNOWN  = 3;
    
    private int responseCode;
    
    private String output;
    
    private double runtime;
    
    private List<NagiosPerfData> perfData = new LinkedList<NagiosPerfData>();
    
    private List<String> additionalOutput = new LinkedList<String>();
    
    public NagiosResult()
    {
        super();
    }
    
    public NagiosResult(Reader stream, int exitCode, double runtime) throws IOException
    {
        super();
        this.parseNagiosOutput(stream, exitCode, runtime);
    }
    
    public NagiosResult(InputStream stream, int exitCode, double runtime) throws IOException
    {
        super();
        this.parseNagiosOutput(stream, exitCode, runtime);
    }
    
    public NagiosResult(String stream, int exitCode, double runtime)
    {
        super();
        this.parseNagiosOutput(stream, exitCode, runtime);
    }
    

    /**
     * The response
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
    
    public boolean isError()
    {
        return this.responseCode < OK || this.responseCode > UNKNOWN;
    }
    
    /**
     * Would Bergamot consider this result to be ok?
     */
    public boolean toOk()
    {
        return this.isOk();
    }
    
    /**
     * What would the Bergamot status be for this result
     */
    public String toStatus()
    {
        switch (this.responseCode)
        {
            case OK:       return "OK";
            case WARNING:  return "WARNING";
            case CRITICAL: return "CRITICAL";
            case UNKNOWN:  return "UNKNOWN";
            default:       return "ERROR";
        }
    }

    /**
     * The output
     */
    public String getOutput()
    {
        return output;
    }

    /**
     * How long the check took to execute in milliseconds
     */
    public double getRuntime()
    {
        return runtime;
    }
    
    /**
     * Get the performance
     */
    public List<NagiosPerfData> getPerfData()
    {
        return this.perfData;
    }
    
    /**
     * Additional output lines
     */
    public List<String> getAdditionalOutput()
    {
        return additionalOutput;
    }
    
    /**
     * Extract the check output
     */
    public NagiosResult parseNagiosOutput(String output, int exitCode, double runtime)
    {
        try
        {
            parseNagiosOutput(new StringReader(output), exitCode, runtime);
        }
        catch (IOException e)
        {
            Logger.getLogger(NagiosResult.class).error("Error parsing nagios output from string", e);
        }
        return this;
    }

    /**
     * Extract the check output
     */
    public NagiosResult parseNagiosOutput(InputStream stream, int exitCode, double runtime) throws IOException
    {
        parseNagiosOutput(new InputStreamReader(stream), exitCode, runtime);
        return this;
    }

    /**
     * Extract the check output
     */
    public NagiosResult parseNagiosOutput(Reader stream, int exitCode, double runtime) throws IOException
    {
        this.responseCode = exitCode;
        this.runtime = runtime;
        // parse the output buffer
        BufferedReader br = new BufferedReader(stream);
        try
        {
            // parse the first line
            String outputLine  = br.readLine();
            if (outputLine != null)
            {
                int pipe = outputLine.indexOf("|");
                if (pipe > 0 && pipe < outputLine.length())
                {
                    this.output = outputLine.substring(0, pipe).trim();
                    String perfData = outputLine.substring(pipe + 1).trim();
                    try
                    {
                        this.perfData.addAll(NagiosPerfData.parsePerfData(perfData));
                    }
                    catch (IOException e)
                    {
                        Logger.getLogger(NagiosResult.class).warn("Failed to parse perfdata, ignoring: " + perfData);
                    }
                }
                else
                {
                    this.output = outputLine.trim();
                }
            }
            // parse any additional lines
            boolean inPerfData = false;
            while ((outputLine = br.readLine()) != null)
            {
                if (inPerfData)
                {
                    String perfData = outputLine.trim();
                    try
                    {
                        this.perfData.addAll(NagiosPerfData.parsePerfData(perfData));
                    }
                    catch (IOException e)
                    {
                        Logger.getLogger(NagiosResult.class).warn("Failed to parse perfdata, ignoring: " + perfData);
                    }
                }
                else
                {
                    int pipe = outputLine.indexOf("|");
                    if (pipe > 0 && pipe < outputLine.length())
                    {
                        this.additionalOutput.add(outputLine.substring(0, pipe).trim());
                        String perfData = outputLine.substring(pipe + 1).trim();
                        try
                        {
                            this.perfData.addAll(NagiosPerfData.parsePerfData(perfData));
                        }
                        catch (IOException e)
                        {
                            Logger.getLogger(NagiosResult.class).warn("Failed to parse perfdata, ignoring: " + perfData);
                        }
                        // we've had start of perf data, treat any additional lines as 
                        // perf data
                        inPerfData = true;
                    }
                    else
                    {
                        this.additionalOutput.add(outputLine.trim());
                    }
                }
            }
        }
        finally
        {
            br.close();
        }
        return this;
    }
    
    public String toString()
    {
        return "Nagios Result: " + this.responseCode + " (" + this.toStatus() + ") " + this.output;
    }
}

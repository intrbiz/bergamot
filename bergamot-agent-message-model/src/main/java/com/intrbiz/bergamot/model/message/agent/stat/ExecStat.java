package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.ParameterMO;
import com.intrbiz.gerald.polyakov.Reading;

/**
 * Result of executing a server defined check
 */
@JsonTypeName("bergamot.agent.stat.exec")
public class ExecStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("status")
    private String status;

    @JsonProperty("output")
    private String output;
    
    @JsonProperty("runtime")
    private double runtime;
    
    /**
     * Metrics captured timestamp
     */
    @JsonProperty("captured")
    private long captured;
    
    /**
     * A collection of metric readings
     */
    @JsonProperty("readings")
    private List<Reading> readings = new LinkedList<Reading>();

    @JsonProperty("parameters")
    private List<ParameterMO> parameters = new LinkedList<ParameterMO>();

    public ExecStat()
    {
        super();
    }

    public ExecStat(Message message)
    {
        super(message);
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public double getRuntime()
    {
        return runtime;
    }

    public void setRuntime(double runtime)
    {
        this.runtime = runtime;
    }

    public List<ParameterMO> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<ParameterMO> parameters)
    {
        this.parameters = parameters;
    }
    
    public long getCaptured()
    {
        return captured;
    }

    public void setCaptured(long captured)
    {
        this.captured = captured;
    }

    public List<Reading> getReadings()
    {
        return readings;
    }

    public void setReadings(List<Reading> readings)
    {
        this.readings = readings;
    }

    @JsonIgnore
    public ExecStat pending(String output)
    {
        this.setOk(true);
        this.setStatus("PENDING");
        this.setOutput(output);
        return this;
    }
    
    @JsonIgnore
    public ExecStat ok(String output)
    {
        this.setOk(true);
        this.setStatus("OK");
        this.setOutput(output);
        return this;
    }
    
    @JsonIgnore
    public ExecStat warning(String output)
    {
        this.setOk(false);
        this.setStatus("WARNING");
        this.setOutput(output);
        return this;
    }
    
    @JsonIgnore
    public ExecStat critical(String output)
    {
        this.setOk(false);
        this.setStatus("CRITICAL");
        this.setOutput(output);
        return this;
    }
    
    @JsonIgnore
    public ExecStat unknown(String output)
    {
        this.setOk(false);
        this.setStatus("UNKNOWN");
        this.setOutput(output);
        return this;
    }

    @JsonIgnore
    public ExecStat error(Throwable t)
    {
        this.setOk(false);
        this.setStatus("ERROR");
        this.setOutput(t.getMessage());
        return this;
    }
    
    @JsonIgnore
    public ExecStat error(String message)
    {
        this.setOk(false);
        this.setStatus("ERROR");
        this.setOutput(message);
        return this;
    }
    
    @JsonIgnore
    public ExecStat timeout(String message)
    {
        this.setOk(false);
        this.setStatus("TIMEOUT");
        this.setOutput(message);
        return this;
    }
}

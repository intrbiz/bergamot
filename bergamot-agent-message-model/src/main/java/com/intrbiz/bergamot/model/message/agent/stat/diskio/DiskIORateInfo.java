package com.intrbiz.bergamot.model.message.agent.stat.diskio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.diskio-rate-info")
public class DiskIORateInfo extends AgentType
{
    /**
     * Read rate in B/s
     */
    @JsonProperty("read-rate")
    private double readRate;

    /**
     * Write rate in B/s
     */
    @JsonProperty("write-rate")
    private double writeRate;
    
    /**
     * Reads /s
     */
    @JsonProperty("reads")
    private double reads;
    
    /**
     * Writes /s
     */
    @JsonProperty("writes")
    private double writes;
    
    /**
     * Peak Read rate in B/s
     */
    @JsonProperty("read-peak-rate")
    private double readPeakRate;

    /**
     * Peak Write rate in B/s
     */
    @JsonProperty("write-peak-rate")
    private double writePeakRate;
    
    /**
     * Peak Reads /s
     */
    @JsonProperty("peak-reads")
    private double peakReads;
    
    /**
     * Peak Writes /s
     */
    @JsonProperty("peak-writes")
    private double peakWrites;

    public DiskIORateInfo()
    {
        super();
    }
    
    public DiskIORateInfo(double readRate, double writeRate, double readPeakRate, double writePeakRate, double reads, double writes, double peakReads, double peakWrites)
    {
        super();
        this.readRate = readRate;
        this.writeRate = writeRate;
        this.readPeakRate = readPeakRate;
        this.writePeakRate = writePeakRate;
        this.reads = reads;
        this.writes = writes;
        this.peakReads = peakReads;
        this.peakWrites = peakWrites;
    }

    public double getReadRate()
    {
        return readRate;
    }

    public void setReadRate(double ReadRate)
    {
        this.readRate = ReadRate;
    }

    public double getWriteRate()
    {
        return writeRate;
    }

    public void setWriteRate(double rxRate)
    {
        this.writeRate = rxRate;
    }
    
    public double getReads()
    {
        return reads;
    }

    public void setReads(double reads)
    {
        this.reads = reads;
    }

    public double getWrites()
    {
        return writes;
    }

    public void setWrites(double writes)
    {
        this.writes = writes;
    }

    public double getReadPeakRate()
    {
        return readPeakRate;
    }

    public void setReadPeakRate(double readPeakRate)
    {
        this.readPeakRate = readPeakRate;
    }

    public double getWritePeakRate()
    {
        return writePeakRate;
    }

    public void setWritePeakRate(double writePeakRate)
    {
        this.writePeakRate = writePeakRate;
    }

    public double getPeakReads()
    {
        return peakReads;
    }

    public void setPeakReads(double peakReads)
    {
        this.peakReads = peakReads;
    }

    public double getPeakWrites()
    {
        return peakWrites;
    }

    public void setPeakWrites(double peakWrites)
    {
        this.peakWrites = peakWrites;
    }
    
    // Read

    @JsonIgnore
    public double getReadRateMBps()
    {
        return this.readRate / 1000000D;
    }
    
    @JsonIgnore
    public double getReadRatekBps()
    {
        return this.readRate / 1000D;
    }
    
    @JsonIgnore
    public double getReadRateBps()
    {
        return this.readRate;
    }
    
    // Write
    
    @JsonIgnore
    public double getWriteRateMBps()
    {
        return this.writeRate / 1000000D;
    }
    
    @JsonIgnore
    public double getWriteRatekBps()
    {
        return this.writeRate / 1000D;
    }
    
    @JsonIgnore
    public double getWriteRateBps()
    {
        return this.writeRate;
    }
    
    // Peak Read
    
    @JsonIgnore
    public double getReadPeakRateMBps()
    {
        return this.readPeakRate / 1000000D;
    }
    
    @JsonIgnore
    public double getReadPeakRatekBps()
    {
        return this.readPeakRate / 1000D;
    }
    
    @JsonIgnore
    public double getReadPeakRateBps()
    {
        return this.readPeakRate;
    }
    
    // Peak Write
    
    @JsonIgnore
    public double getWritePeakRateMBps()
    {
        return this.writePeakRate / 1000000D;
    }
    
    @JsonIgnore
    public double getWritePeakRatekBps()
    {
        return this.writePeakRate / 1000D;
    }
    
    @JsonIgnore
    public double getWritePeakRateBps()
    {
        return this.writePeakRate;
    }
}

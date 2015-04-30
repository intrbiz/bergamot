package com.intrbiz.bergamot.model.message.agent.stat.netio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.netio-rate-info")
public class NetIORateInfo extends AgentType
{
    /**
     * Transmit rate in B/s
     */
    @JsonProperty("tx-rate")
    private double txRate;

    /**
     * Receive rate in B/s
     */
    @JsonProperty("rx-rate")
    private double rxRate;
    
    /**
     * Peak Transmit rate in B/s
     */
    @JsonProperty("tx-peak-rate")
    private double txPeakRate;

    /**
     * Peak Receive rate in B/s
     */
    @JsonProperty("rx-peak-rate")
    private double rxPeakRate;

    public NetIORateInfo()
    {
        super();
    }
    
    public NetIORateInfo(double txRate, double rxRate, double txPeakRate, double rxPeakRate)
    {
        super();
        this.txRate = txRate;
        this.rxRate = rxRate;
        this.txPeakRate = txPeakRate;
        this.rxPeakRate = rxPeakRate;
    }

    public double getTxRate()
    {
        return txRate;
    }

    public void setTxRate(double txRate)
    {
        this.txRate = txRate;
    }

    public double getRxRate()
    {
        return rxRate;
    }

    public void setRxRate(double rxRate)
    {
        this.rxRate = rxRate;
    }
    
    public double getTxPeakRate()
    {
        return txPeakRate;
    }

    public void setTxPeakRate(double txPeakRate)
    {
        this.txPeakRate = txPeakRate;
    }

    public double getRxPeakRate()
    {
        return rxPeakRate;
    }

    public void setRxPeakRate(double rxPeakRate)
    {
        this.rxPeakRate = rxPeakRate;
    }
    
    // TX

    @JsonIgnore
    public double getTxRateMBps()
    {
        return this.txRate / 1000000D;
    }
    
    @JsonIgnore
    public double getTxRatekBps()
    {
        return this.txRate / 1000D;
    }
    
    @JsonIgnore
    public double getTxRateBps()
    {
        return this.txRate;
    }
    
    @JsonIgnore
    public double getTxRateMbps()
    {
        return (this.txRate * 8D) / 1000000D;
    }
    
    @JsonIgnore
    public double getTxRatekbps()
    {
        return (this.txRate * 8D) / 1000D;
    }
    
    @JsonIgnore
    public double getTxRatebps()
    {
        return this.txRate * 8D;
    }
    
    // RX
    
    @JsonIgnore
    public double getRxRateMBps()
    {
        return this.rxRate / 1000000D;
    }
    
    @JsonIgnore
    public double getRxRatekBps()
    {
        return this.rxRate / 1000D;
    }
    
    @JsonIgnore
    public double getRxRateBps()
    {
        return this.rxRate;
    }
    
    @JsonIgnore
    public double getRxRateMbps()
    {
        return (this.rxRate * 8D) / 1000000D;
    }
    
    @JsonIgnore
    public double getRxRatekbps()
    {
        return (this.rxRate * 8D) / 1000D;
    }
    
    @JsonIgnore
    public double getRxRatebps()
    {
        return this.rxRate * 8D;
    }
    
    // Peak
    
    // TX
    
    @JsonIgnore
    public double getTxPeakRateMBps()
    {
        return this.txPeakRate / 1000000D;
    }
    
    @JsonIgnore
    public double getTxPeakRatekBps()
    {
        return this.txPeakRate / 1000D;
    }
    
    @JsonIgnore
    public double getTxPeakRateBps()
    {
        return this.txPeakRate;
    }
    
    @JsonIgnore
    public double getTxPeakRateMbps()
    {
        return this.txPeakRate / 100000D;
    }
    
    @JsonIgnore
    public double getTxPeakRatekbps()
    {
        return this.txPeakRate / 100D;
    }
    
    @JsonIgnore
    public double getTxPeakRatebps()
    {
        return this.txPeakRate * 10D;
    }
    
    // RX
    
    @JsonIgnore
    public double getRxPeakRateMBps()
    {
        return this.rxPeakRate / 1000000D;
    }
    
    @JsonIgnore
    public double getRxPeakRatekBps()
    {
        return this.rxPeakRate / 1000D;
    }
    
    @JsonIgnore
    public double getRxPeakRateBps()
    {
        return this.rxPeakRate;
    }
    
    @JsonIgnore
    public double getRxPeakRateMbps()
    {
        return this.rxPeakRate / 100000D;
    }
    
    @JsonIgnore
    public double getRxPeakRatekbps()
    {
        return this.rxPeakRate / 100D;
    }
    
    @JsonIgnore
    public double getRxPeakRatebps()
    {
        return this.rxPeakRate * 10D;
    }
}

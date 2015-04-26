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

    public NetIORateInfo()
    {
        super();
    }
    
    public NetIORateInfo(double txRate, double rxRate)
    {
        super();
        this.txRate = txRate;
        this.rxRate = rxRate;
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
        return this.txRate / 100000D;
    }
    
    @JsonIgnore
    public double getTxRatekbps()
    {
        return this.txRate / 100D;
    }
    
    @JsonIgnore
    public double getTxRatebps()
    {
        return this.txRate * 10D;
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
        return this.rxRate / 100000D;
    }
    
    @JsonIgnore
    public double getRxRatekbps()
    {
        return this.rxRate / 100D;
    }
    
    @JsonIgnore
    public double getRxRatebps()
    {
        return this.rxRate * 10D;
    }
}
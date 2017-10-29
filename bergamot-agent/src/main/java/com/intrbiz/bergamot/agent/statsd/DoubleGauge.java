package com.intrbiz.bergamot.agent.statsd;

import com.codahale.metrics.Gauge;

public class DoubleGauge implements Gauge<Double>
{
    private double value = 0;
    
    public DoubleGauge()
    {
        super();
    }
    
    public void set(double value)
    {
        this.value = value;
    }
    
    public void add(double value)
    {
        this.value = this.value + value;
    }

    @Override
    public Double getValue()
    {
        return this.value;
    }
}

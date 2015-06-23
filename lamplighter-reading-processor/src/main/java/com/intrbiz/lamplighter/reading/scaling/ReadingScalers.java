package com.intrbiz.lamplighter.reading.scaling;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.gerald.polyakov.Reading;

public class ReadingScalers
{
    private ConcurrentMap<String, ReadingScaler> scalers = new ConcurrentHashMap<String, ReadingScaler>();
    
    public ReadingScalers()
    {
        super();
        // default scalers
        this.registerScaler(new BytesScaler());
        this.registerScaler(new SecondsScaler());
        this.registerScaler(new BytesRateScaler());
        this.registerScaler(new BitRateScaler());
    }
    
    public void registerScaler(ReadingScaler scaler)
    {
        for (String unit : scaler.getUnits())
        {
            this.scalers.put(unit, scaler);
        }
    }
    
    public ReadingScaler build(String unit)
    {
        return this.scalers.get(unit);
    }
    
    public Reading scale(Reading reading, String toUnit)
    {
        if (reading.getUnit() != null)
        {
            ReadingScaler scaler = this.build(reading.getUnit());
            if (scaler != null) return scaler.scaleReading(reading, toUnit);
        }
        return reading;
    }
}

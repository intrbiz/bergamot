package com.intrbiz.lamplighter.reading.scaling;

import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

/**
 * Scale microseconds, milliseconds and seconds to nanoseconds
 */
public class SecondsScaler implements ReadingScaler
{
    @Override
    public String[] getUnits()
    {
        return new String[] { "ns", "us", "ms", "s" };
    }
    
    public static double toNanos(String unit)
    {
        switch (unit)
        {
            case "ns" : return 1D;
            case "us" : return 1000D;
            case "ms" : return 1000D * 1000D;
            case "s"  : return 1000D * 1000D * 1000D;
        }
        throw new IllegalArgumentException("Unknown unit: " + unit);
    }

    @Override
    public Reading scaleReading(Reading reading, String toUnit)
    {
        if (reading instanceof LongGaugeReading)
        {
            return this.scale((LongGaugeReading) reading, toUnit);
        }
        else if (reading instanceof DoubleGaugeReading)
        {
            return this.scale((DoubleGaugeReading) reading, toUnit);
        }
        else if (reading instanceof IntegerGaugeReading)
        {
            return this.scale((IntegerGaugeReading) reading, toUnit);
        }
        else if (reading instanceof FloatGaugeReading)
        {
            return this.scale((FloatGaugeReading) reading, toUnit);
        }
        return reading;
    }
    
    protected Reading scale(FloatGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toNanos(reading.getUnit()) / toNanos(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue((float) (reading.getValue()       * factor));
            if (reading.getWarning() != null)  reading.setWarning((float) (reading.getWarning()   * factor));
            if (reading.getCritical() != null) reading.setCritical((float) (reading.getCritical() * factor));
            if (reading.getMin() != null)      reading.setMin((float) (reading.getMin()           * factor));
            if (reading.getMax() != null)      reading.setMax((float) (reading.getMax()           * factor));
        }
        return reading;
    }

    protected Reading scale(DoubleGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toNanos(reading.getUnit()) / toNanos(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * factor);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * factor);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * factor);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * factor);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * factor);
        }
        return reading;
    }
    
    protected Reading scale(IntegerGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toNanos(reading.getUnit()) / toNanos(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue((int) (reading.getValue()       * factor));
            if (reading.getWarning() != null)  reading.setWarning((int) (reading.getWarning()   * factor));
            if (reading.getCritical() != null) reading.setCritical((int) (reading.getCritical() * factor));
            if (reading.getMin() != null)      reading.setMin((int) (reading.getMin()           * factor));
            if (reading.getMax() != null)      reading.setMax((int) (reading.getMax()           * factor));
        }
        return reading;
    }
    
    protected Reading scale(LongGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toNanos(reading.getUnit()) / toNanos(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue((long) (reading.getValue()       * factor));
            if (reading.getWarning() != null)  reading.setWarning((long) (reading.getWarning()   * factor));
            if (reading.getCritical() != null) reading.setCritical((long) (reading.getCritical() * factor));
            if (reading.getMin() != null)      reading.setMin((long) (reading.getMin()           * factor));
            if (reading.getMax() != null)      reading.setMax((long) (reading.getMax()           * factor));
        }
        return reading;
    }
}

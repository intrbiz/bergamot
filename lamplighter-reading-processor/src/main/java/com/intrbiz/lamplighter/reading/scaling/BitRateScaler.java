package com.intrbiz.lamplighter.reading.scaling;

import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

/**
 * Scale kb/s, Mb/s, Gb/s, Tb/s, Kib/s, Mib/s, Gib/s, Tib/s
 */
public class BitRateScaler implements ReadingScaler
{
    @Override
    public String[] getUnits()
    {
        return new String[] { "b/s", "kb/s", "Kb/s", "Mb/s", "Gb/s", "Tb/s", "Kib/s", "Mib/s", "Gib/s", "Tib/s" };
    }
    
    public static double toBytes(String unit)
    {
        switch (unit)
        {
            case "b/s"   : return 1D;
            case "kb/s"  : return 1000D;
            case "Kb/s"  : return 1000D;
            case "Mb/s"  : return 1000D * 1000D;
            case "Gb/s"  : return 1000D * 1000D * 1000D;
            case "Tb/s"  : return 1000D * 1000D * 1000D * 1000D;
            case "Kib/s" : return 1024D;
            case "Mib/s" : return 1024D * 1024D;
            case "Gib/s" : return 1024D * 1024D * 1024D;
            case "Tib/s" : return 1024D * 1024D * 1024D * 1024D;
        }
        throw new IllegalArgumentException("Unknown unit: " + unit);
    }

    @Override
    public <T extends Reading> T scaleReading(T reading, String toUnit)
    {
        if (reading instanceof LongGaugeReading)
        {
            this.scale((LongGaugeReading) reading, toUnit);
        }
        else if (reading instanceof DoubleGaugeReading)
        {
            this.scale((DoubleGaugeReading) reading, toUnit);
        }
        else if (reading instanceof IntegerGaugeReading)
        {
            this.scale((IntegerGaugeReading) reading, toUnit);
        }
        else if (reading instanceof FloatGaugeReading)
        {
            this.scale((FloatGaugeReading) reading, toUnit);
        }
        return reading;
    }
    
    protected void scale(FloatGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toBytes(reading.getUnit()) / toBytes(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue((float) (reading.getValue()       * factor));
            if (reading.getWarning() != null)  reading.setWarning((float) (reading.getWarning()   * factor));
            if (reading.getCritical() != null) reading.setCritical((float) (reading.getCritical() * factor));
            if (reading.getMin() != null)      reading.setMin((float) (reading.getMin()           * factor));
            if (reading.getMax() != null)      reading.setMax((float) (reading.getMax()           * factor));
        }
    }

    protected void scale(DoubleGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toBytes(reading.getUnit()) / toBytes(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * factor);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * factor);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * factor);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * factor);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * factor);
        }
    }
    
    protected void scale(IntegerGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toBytes(reading.getUnit()) / toBytes(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue((int) (reading.getValue()       * factor));
            if (reading.getWarning() != null)  reading.setWarning((int) (reading.getWarning()   * factor));
            if (reading.getCritical() != null) reading.setCritical((int) (reading.getCritical() * factor));
            if (reading.getMin() != null)      reading.setMin((int) (reading.getMin()           * factor));
            if (reading.getMax() != null)      reading.setMax((int) (reading.getMax()           * factor));
        }
    }
    
    protected void scale(LongGaugeReading reading, String toUnit)
    {
        if (! toUnit.equals(reading.getUnit()))
        {
            // work out the scale factor
            double factor = toBytes(reading.getUnit()) / toBytes(toUnit);
            // scale
            reading.setUnit(toUnit);
            if (reading.getValue() != null)    reading.setValue((long) (reading.getValue()       * factor));
            if (reading.getWarning() != null)  reading.setWarning((long) (reading.getWarning()   * factor));
            if (reading.getCritical() != null) reading.setCritical((long) (reading.getCritical() * factor));
            if (reading.getMin() != null)      reading.setMin((long) (reading.getMin()           * factor));
            if (reading.getMax() != null)      reading.setMax((long) (reading.getMax()           * factor));
        }
    }
}

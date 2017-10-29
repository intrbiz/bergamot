package com.intrbiz.lamplighter.reading.scaling;

import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

/**
 * Scale B kB, MB, GB, TB, KiB, MiB, GiB, TiB
 */
public class BytesScaler implements ReadingScaler
{
    @Override
    public String[] getUnits()
    {
        return new String[] { "B", "kB", "KB", "MB", "GB", "TB", "KiB", "MiB", "GiB", "TiB" };
    }
    
    public static double toBytes(String unit)
    {
        switch (unit)
        {
            case "B"   : return 1D;
            case "kB"  : return 1000D;
            case "KB"  : return 1000D;
            case "MB"  : return 1000D * 1000D;
            case "GB"  : return 1000D * 1000D * 1000D;
            case "TB"  : return 1000D * 1000D * 1000D * 1000D;
            case "KiB" : return 1024D;
            case "MiB" : return 1024D * 1024D;
            case "GiB" : return 1024D * 1024D * 1024D;
            case "TiB" : return 1024D * 1024D * 1024D * 1024D;
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

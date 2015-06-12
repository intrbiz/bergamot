package com.intrbiz.lamplighter.reading.scaling;

import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

/**
 * Scale KiB, MiB, GiB, TiB to Bytes.  Note base 2.
 */
public class Base2BytesScaler implements ReadingScaler
{
    private static final double KiB_D = 1024D;
    private static final double MiB_D = 1024D * 1024D;
    private static final double GiB_D = 1024D * 1024D * 1024D;
    private static final double TiB_D = 1024D * 1024D * 1024D * 1024D;
    
    private static final long KiB_L = 1024L;
    private static final long MiB_L = 1024L * 1024L;
    private static final long GiB_L = 1024L * 1024L * 1024L;
    private static final long TiB_L = 1024L * 1024L * 1024L * 1024L;

    @Override
    public String[] getUnits()
    {
        return new String[] { "KiB", "MiB", "GiB", "TiB" };
    }

    @Override
    public Reading scaleReading(Reading reading)
    {
        if (reading instanceof LongGaugeReading)
        {
            return this.scaleToBaseUnits((LongGaugeReading) reading);
        }
        else if (reading instanceof DoubleGaugeReading)
        {
            return this.scaleToBaseUnits((DoubleGaugeReading) reading);
        }
        else if (reading instanceof IntegerGaugeReading)
        {
            return this.scaleToBaseUnits((IntegerGaugeReading) reading);
        }
        else if (reading instanceof FloatGaugeReading)
        {
            return this.scaleToBaseUnits((FloatGaugeReading) reading);
        }
        return reading;
    }
    
    protected Reading scaleToBaseUnits(FloatGaugeReading reading)
    {
        // promote to double
        DoubleGaugeReading dgr = new DoubleGaugeReading();
        dgr.setName(reading.getName());
        dgr.setUnit(reading.getUnit());
        dgr.setValue(reading.getValue() == null ? null : reading.getValue().doubleValue());
        dgr.setWarning(reading.getValue() == null ? null : reading.getWarning().doubleValue());
        dgr.setCritical(reading.getValue() == null ? null : reading.getCritical().doubleValue());
        dgr.setMin(reading.getValue() == null ? null : reading.getMin().doubleValue());
        dgr.setMax(reading.getValue() == null ? null : reading.getMax().doubleValue());
        // scale
        return this.scaleToBaseUnits(dgr);
    }

    protected Reading scaleToBaseUnits(DoubleGaugeReading reading)
    {
        if ("kB".equals(reading.getUnit()) || "KB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * KiB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * KiB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * KiB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * KiB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * KiB_D);
        }
        else if ("MB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * MiB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * MiB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * MiB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * MiB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * MiB_D);
        }
        else if ("GB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * GiB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * GiB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * GiB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * GiB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * GiB_D);
        }
        else if ("TB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * TiB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * TiB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * TiB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * TiB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * TiB_D);
        }
        return reading;
    }
    
    protected Reading scaleToBaseUnits(IntegerGaugeReading reading)
    {
        // promote to double
        LongGaugeReading dgr = new LongGaugeReading();
        dgr.setName(reading.getName());
        dgr.setUnit(reading.getUnit());
        dgr.setValue(reading.getValue() == null ? null : reading.getValue().longValue());
        dgr.setWarning(reading.getValue() == null ? null : reading.getWarning().longValue());
        dgr.setCritical(reading.getValue() == null ? null : reading.getCritical().longValue());
        dgr.setMin(reading.getValue() == null ? null : reading.getMin().longValue());
        dgr.setMax(reading.getValue() == null ? null : reading.getMax().longValue());
        // scale
        return this.scaleToBaseUnits(dgr);
    }
    
    protected Reading scaleToBaseUnits(LongGaugeReading reading)
    {
        if ("kB".equals(reading.getUnit()) || "KB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * KiB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * KiB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * KiB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * KiB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * KiB_L);
        }
        else if ("MB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * MiB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * MiB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * MiB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * MiB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * MiB_L);
        }
        else if ("GB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * GiB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * GiB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * GiB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * GiB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * GiB_L);
        }
        else if ("TB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * TiB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * TiB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * TiB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * TiB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * TiB_L);
        }
        return reading;
    }
}

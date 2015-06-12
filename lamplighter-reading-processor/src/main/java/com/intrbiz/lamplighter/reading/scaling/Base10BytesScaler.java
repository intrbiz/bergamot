package com.intrbiz.lamplighter.reading.scaling;

import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

/**
 * Scale kB, MB, GB, TB to Bytes.  Note base 10.
 */
public class Base10BytesScaler implements ReadingScaler
{
    private static final double kB_D = 1000D;
    private static final double MB_D = 1000D * 1000D;
    private static final double GB_D = 1000D * 1000D * 1000D;
    private static final double TB_D = 1000D * 1000D * 1000D * 1000D;
    
    private static final long kB_L = 1000L;
    private static final long MB_L = 1000L * 1000L;
    private static final long GB_L = 1000L * 1000L * 1000L;
    private static final long TB_L = 1000L * 1000L * 1000L * 1000L;

    @Override
    public String[] getUnits()
    {
        return new String[] { "kB", "KB", "MB", "GB", "TB" };
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
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * kB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * kB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * kB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * kB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * kB_D);
        }
        else if ("MB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * MB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * MB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * MB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * MB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * MB_D);
        }
        else if ("GB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * GB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * GB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * GB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * GB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * GB_D);
        }
        else if ("TB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * TB_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * TB_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * TB_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * TB_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * TB_D);
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
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * kB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * kB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * kB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * kB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * kB_L);
        }
        else if ("MB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * MB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * MB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * MB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * MB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * MB_L);
        }
        else if ("GB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * GB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * GB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * GB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * GB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * GB_L);
        }
        else if ("TB".equals(reading.getUnit()))
        {
            reading.setUnit("B");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * TB_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * TB_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * TB_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * TB_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * TB_L);
        }
        return reading;
    }
}

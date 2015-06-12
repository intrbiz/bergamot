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
    private static final double us_D = 1000D;
    private static final double ms_D = 1000D * 1000D;
    private static final double s_D  = 1000D * 1000D * 1000D;
    
    private static final long us_L = 1000L;
    private static final long ms_L = 1000L * 1000L;
    private static final long s_L  = 1000L * 1000L * 1000L;

    @Override
    public String[] getUnits()
    {
        return new String[] { "us", "ms", "s" };
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
        if ("us".equals(reading.getUnit()))
        {
            reading.setUnit("ns");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * us_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * us_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * us_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * us_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * us_D);
        }
        else if ("ms".equals(reading.getUnit()))
        {
            reading.setUnit("ns");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * ms_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * ms_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * ms_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * ms_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * ms_D);
        }
        else if ("s".equals(reading.getUnit()))
        {
            reading.setUnit("ns");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * s_D);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * s_D);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * s_D);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * s_D);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * s_D);
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
        if ("us".equals(reading.getUnit()))
        {
            reading.setUnit("ns");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * us_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * us_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * us_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * us_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * us_L);
        }
        else if ("ms".equals(reading.getUnit()))
        {
            reading.setUnit("ns");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * ms_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * ms_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * ms_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * ms_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * ms_L);
        }
        else if ("s".equals(reading.getUnit()))
        {
            reading.setUnit("ns");
            if (reading.getValue() != null)    reading.setValue(reading.getValue()       * s_L);
            if (reading.getWarning() != null)  reading.setWarning(reading.getWarning()   * s_L);
            if (reading.getCritical() != null) reading.setCritical(reading.getCritical() * s_L);
            if (reading.getMin() != null)      reading.setMin(reading.getMin()           * s_L);
            if (reading.getMax() != null)      reading.setMax(reading.getMax()           * s_L);
        }
        return reading;
    }
}

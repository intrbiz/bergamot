package com.intrbiz.bergamot.worker.engine.script;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.Executor;
import com.intrbiz.gerald.polyakov.Reading;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.FloatGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class ActiveCheckScriptContext
{
    private static final String WARNING = "warning";
    
    private static final String CRITICAL = "critical";
    
    private final ExecuteCheck executeCheck;
    
    private final Executor<?> executor;
    
    private final long start = System.currentTimeMillis();
    
    public ActiveCheckScriptContext(ExecuteCheck executeCheck, Executor<?> executor)
    {
        this.executeCheck = executeCheck;
        this.executor = executor;
    }

    public ExecuteCheck getExecuteCheck()
    {
        return executeCheck;
    }
    
    public ExecuteCheck getCheck()
    {
        return executeCheck;
    }
    
    // simple results
    
    public void info(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).info(message));
    }
    
    public void ok(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).ok(message));
    }
    
    public void warning(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).warning(message));
    }
    
    public void critical(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).critical(message));
    }
    
    public void unknown(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).unknown(message));
    }
    
    public void error(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).error(message));
    }
    
    public void error(Throwable error)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).error(error));
    }
    
    public void timeout(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).timeout(message));
    }
    
    public void disconnected(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).disconnected(message));
    }
    
    public void action(String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).action(message));
    }
    
    // generic thresholds
    
    public <V,T> void applyThreshold(V value, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyThreshold(value, match, warning, critical, message));
    }

    public <V,T> void applyThresholds(Iterable<V> values, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyThresholds(values, match, warning, critical, message));
    }
    
    // double thresholds
    
    public void applyGreaterThanThreshold(Double value, String message)
    {
        this.require(WARNING, CRITICAL);
        double warning = this.executeCheck.getDoubleParameter(WARNING);
        double critical = this.executeCheck.getDoubleParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Double value, Double warning, Double critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Double value, String message)
    {
        this.require(WARNING, CRITICAL);
        double warning = this.executeCheck.getDoubleParameter(WARNING);
        double critical = this.executeCheck.getDoubleParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Double value, Double warning, Double critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    // float thresholds
    
    public void applyGreaterThanThreshold(Float value, String message)
    {
        this.require(WARNING, CRITICAL);
        float warning = this.executeCheck.getFloatParameter(WARNING);
        float critical = this.executeCheck.getFloatParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Float value, Float warning, Float critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Float value, String message)
    {
        this.require(WARNING, CRITICAL);
        float warning = this.executeCheck.getFloatParameter(WARNING);
        float critical = this.executeCheck.getFloatParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Float value, Float warning, Float critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    // long thresholds
    
    public void applyGreaterThanThreshold(Long value, String message)
    {
        this.require(WARNING, CRITICAL);
        long warning = this.executeCheck.getLongParameter(WARNING);
        long critical = this.executeCheck.getLongParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Long value, Long warning, Long critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Long value, String message)
    {
        this.require(WARNING, CRITICAL);
        long warning = this.executeCheck.getLongParameter(WARNING);
        long critical = this.executeCheck.getLongParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Long value, Long warning, Long critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    // int thresholds
    
    public void applyGreaterThanThreshold(Integer value, String message)
    {
        this.require(WARNING, CRITICAL);
        int warning = this.executeCheck.getIntParameter(WARNING);
        int critical = this.executeCheck.getIntParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Integer value, String message)
    {
        this.require(WARNING, CRITICAL);
        int warning = this.executeCheck.getIntParameter(WARNING);
        int critical = this.executeCheck.getIntParameter(CRITICAL);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    // collection thresholds
    
    public void applyGreaterThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThresholds(values, warning, critical, message));
    }
    
    public void applyLessThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThresholds(values, warning, critical, message));
    }
    
    public void applyGreaterThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThresholds(values, warning, critical, message));
    }
    
    public void applyLessThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThresholds(values, warning, critical, message));
    }
    
    public void applyGreaterThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThresholds(values, warning, critical, message));
    }
    
    public void applyLessThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThresholds(values, warning, critical, message));
    }
    
    public void applyGreaterThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThresholds(values, warning, critical, message));
    }
    
    public void applyLessThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThresholds(values, warning, critical, message));
    }
    
    // ranges
    
    public <V,T> void applyRange(V value, BiPredicate<V,T> lowerMatch, BiPredicate<V,T> upperMatch, T[] warning, T[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, lowerMatch, upperMatch, warning, critical, message));
    }
    
    public void applyRange(Long value, String message)
    {
        this.require(WARNING, CRITICAL);
        Long[] warning = this.executeCheck.getLongRangeParameter(WARNING, (Long[]) null);
        Long[] critical = this.executeCheck.getLongRangeParameter(CRITICAL, (Long[]) null);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Long value, Long[] warning, Long[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Integer value, String message)
    {
        this.require(WARNING, CRITICAL);
        Integer[] warning = this.executeCheck.getIntRangeParameter(WARNING, (Integer[]) null);
        Integer[] critical = this.executeCheck.getIntRangeParameter(CRITICAL, (Integer[]) null);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Integer value, Integer[] warning, Integer[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Double value, String message)
    {
        this.require(WARNING, CRITICAL);
        Double[] warning = this.executeCheck.getDoubleRangeParameter(WARNING, (Double[]) null);
        Double[] critical = this.executeCheck.getDoubleRangeParameter(CRITICAL, (Double[]) null);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Double value, Double[] warning, Double[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Float value, String message)
    {
        this.require(WARNING, CRITICAL);
        Float[] warning = this.executeCheck.getFloatRangeParameter(WARNING, (Float[]) null);
        Float[] critical = this.executeCheck.getFloatRangeParameter(CRITICAL, (Float[]) null);
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Float value, Float[] warning, Float[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    // generic result publish
    
    public void publish(ActiveResultMO resultMO)
    {
        resultMO.runtime(System.currentTimeMillis() - this.start);
        this.executor.publishActiveResult(this.executeCheck, resultMO);
    }
    
    public ActiveResultMO createResult()
    {
        return new ActiveResultMO().fromCheck(this.getCheck());
    }
    
    // parameter validation
    
    public void require(String... parameterNames)
    {
        for (String param : parameterNames)
        {
            this.require(param);
        }
    }
    
    public void require(List<String> parameterNames)
    {
        for (String param : parameterNames)
        {
            this.require(param);
        }
    }
    
    public void require(String parameterName)
    {
        if (Util.isEmpty(this.getCheck().getParameter(parameterName))) throw new RuntimeException("The " + parameterName + " parameter must be defined!");
    }
    
    // generic requirements
    
    public void require(boolean check, String message)
    {
        if (check) throw new RuntimeException(message);
    }
    
    // readings
    
    public void publishReadings(List<ScriptObjectMirror> readings)
    {
        // map the JSObjects into a reading
        List<Reading> realReadings = new LinkedList<Reading>();
        for (ScriptObjectMirror reading : readings)
        {
            Reading realReading = this.createReading(reading);
            if (realReading != null) 
                realReadings.add(realReading);
        }
        System.out.println(realReadings);
        // publish the readings
        if (! realReadings.isEmpty())
            this.executor.publishReading(this.executeCheck, realReadings);
    }
    
    /**
     * Create a reading from a JavaScript reading definition:
     * { type: 'long', name: 'test.gauge', unit: 'MB', value: 0, warning: 0, critical: 0, min: 0, max: 0 }
     */
    public Reading createReading(ScriptObjectMirror jsReading)
    {
        String type = (String) jsReading.get("type");
        if (Util.isEmpty(type)) return null;
        String name = (String) jsReading.get("name");
        if (Util.isEmpty(name)) return null;
        String unit = (String) jsReading.get("unit");
        // get the possible values
        Number value = (Number) jsReading.get("value");
        Number warning = (Number) jsReading.get("warning");
        Number critical = (Number) jsReading.get("critical");
        Number min = (Number) jsReading.get("min");
        Number max = (Number) jsReading.get("max");
        // map the reading
        switch (type.toLowerCase())
        {
            case "double":
            case "double-gauge":
                return createDoubleGaugeReading(name, unit, value, warning, critical, min, max);
            case "long":
            case "long-gauge":
            case "int8":
            case "int8-gauge":
                return createLongGaugeReading(name, unit, value, warning, critical, min, max);
            case "float":
            case "float-gauge":
            case "real":
            case "real-gauge":
                return createFloatGaugeReading(name, unit, value, warning, critical, min, max);
            case "integer":
            case "integer-gauge":
            case "int":
            case "int-gauge":
            case "int4":
            case "int4-gauge":
                return createIntegerGaugeReading(name, unit, value, warning, critical, min, max);
        }
        return null;
    }
    
    public void publishReadings(Reading... readings)
    {
        if (readings != null && readings.length > 0)
            this.executor.publishReading(this.executeCheck, readings);
    }
    
    public void publishReadings(ReadingParcelMO readings)
    {
        if (readings != null)
            this.executor.publishReading(this.executeCheck, readings);
    }
    
    public DoubleGaugeReading createDoubleGaugeReading(String name, String unit, Number value)
    {
        return new DoubleGaugeReading(
                name, 
                unit, 
                value == null ? null : value.doubleValue()
        );
    }
    
    public DoubleGaugeReading createDoubleGaugeReading(String name, String unit, Number value, Number warning, Number critical, Number min, Number max)
    {
        return new DoubleGaugeReading(
                name, 
                unit, 
                value == null ? null : value.doubleValue(), 
                warning == null ? null : warning.doubleValue(), 
                critical == null ? null : critical.doubleValue(), 
                min == null ? null : min.doubleValue(), 
                max == null ? null : max.doubleValue()
        );
    }
    
    public LongGaugeReading createLongGaugeReading(String name, String unit, Number value)
    {
        return new LongGaugeReading(
                name, 
                unit, 
                value == null ? null : value.longValue()
        );
    }
    
    public LongGaugeReading createLongGaugeReading(String name, String unit, Number value, Number warning, Number critical, Number min, Number max)
    {
        return new LongGaugeReading(
                name, 
                unit, 
                value == null ? null : value.longValue(), 
                warning == null ? null : warning.longValue(), 
                critical == null ? null : critical.longValue(), 
                min == null ? null : min.longValue(), 
                max == null ? null : max.longValue()
        );
    }
    
    public IntegerGaugeReading createIntegerGaugeReading(String name, String unit, Number value)
    {
        return new IntegerGaugeReading(
                name, 
                unit, 
                value == null ? null : value.intValue()
         );
    }
    
    public IntegerGaugeReading createIntegerGaugeReading(String name, String unit, Number value, Number warning, Number critical, Number min, Number max)
    {
        return new IntegerGaugeReading(
                name, 
                unit, 
                value == null ? null : value.intValue(), 
                warning == null ? null : warning.intValue(), 
                critical == null ? null : critical.intValue(), 
                min == null ? null : min.intValue(), 
                max == null ? null : max.intValue()
         );
    }
    
    public FloatGaugeReading createFloatGaugeReading(String name, String unit, Number value)
    {
        return new FloatGaugeReading(
                name, 
                unit, 
                value == null ? null : value.floatValue()
        );
    }
    
    public FloatGaugeReading createFloatGaugeReading(String name, String unit, Number value, Number warning, Number critical, Number min, Number max)
    {
        return new FloatGaugeReading(
                name, 
                unit, 
                value == null ? null : value.floatValue(), 
                warning == null ? null : warning.floatValue(), 
                critical == null ? null : critical.floatValue(), 
                min == null ? null : min.floatValue(), 
                max == null ? null : max.floatValue()
         );
    }
}

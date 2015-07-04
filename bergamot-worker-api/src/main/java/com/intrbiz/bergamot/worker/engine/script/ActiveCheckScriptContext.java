package com.intrbiz.bergamot.worker.engine.script;

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

public class ActiveCheckScriptContext
{
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
    
    public <V,T> void applyThreshold(V value, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyThreshold(value, match, warning, critical, message));
    }

    public <V,T> void applyThresholds(Iterable<V> values, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyThresholds(values, match, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Double value, Double warning, Double critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Double value, Double warning, Double critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Float value, Float warning, Float critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Float value, Float warning, Float critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Long value, Long warning, Long critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Long value, Long warning, Long critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
    public void applyGreaterThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyGreaterThanThreshold(value, warning, critical, message));
    }
    
    public void applyLessThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyLessThanThreshold(value, warning, critical, message));
    }
    
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
    
    public <V,T> void applyRange(V value, BiPredicate<V,T> lowerMatch, BiPredicate<V,T> upperMatch, T[] warning, T[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, lowerMatch, upperMatch, warning, critical, message));
    }
    
    public void applyRange(Long value, Long[] warning, Long[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Integer value, Integer[] warning, Integer[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Double value, Double[] warning, Double[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void applyRange(Float value, Float[] warning, Float[] critical, String message)
    {
        this.publish(new ActiveResultMO().fromCheck(this.executeCheck).applyRange(value, warning, critical, message));
    }
    
    public void publish(ActiveResultMO resultMO)
    {
        resultMO.runtime(System.currentTimeMillis() - this.start);
        this.executor.publishActiveResult(this.executeCheck, resultMO);
    }
    
    public ActiveResultMO createResult()
    {
        return new ActiveResultMO().fromCheck(this.getCheck());
    }
    
    public void require(String parameterName)
    {
        if (Util.isEmpty(this.getCheck().getParameter(parameterName))) throw new RuntimeException("The " + parameterName + " parameter must be defined!");
    }
    
    public void require(boolean check, String message)
    {
        if (check) throw new RuntimeException(message);
    }
    
    // readings
    
    public void publishReadings(Reading... readings)
    {
        this.executor.publishReading(this.executeCheck, readings);
    }
    
    public void publishReadings(ReadingParcelMO readings)
    {
        this.executor.publishReading(this.executeCheck, readings);
    }
    
    public DoubleGaugeReading createDoubleGaugeReading(String name, String unit, Double value)
    {
        return new DoubleGaugeReading(name, unit, value);
    }
    
    public DoubleGaugeReading createDoubleGaugeReading(String name, String unit, Double value, Double warning, Double critical, Double min, Double max)
    {
        return new DoubleGaugeReading(name, unit, value, warning, critical, min, max);
    }
    
    public LongGaugeReading createLongGaugeReading(String name, String unit, Long value)
    {
        return new LongGaugeReading(name, unit, value);
    }
    
    public LongGaugeReading createDoubleGaugeReading(String name, String unit, Long value, Long warning, Long critical, Long min, Long max)
    {
        return new LongGaugeReading(name, unit, value, warning, critical, min, max);
    }
    
    public IntegerGaugeReading createIntegerGaugeReading(String name, String unit, Integer value)
    {
        return new IntegerGaugeReading(name, unit, value);
    }
    
    public IntegerGaugeReading createDoubleGaugeReading(String name, String unit, Integer value, Integer warning, Integer critical, Integer min, Integer max)
    {
        return new IntegerGaugeReading(name, unit, value, warning, critical, min, max);
    }
    
    public FloatGaugeReading createFloatGaugeReading(String name, String unit, Float value)
    {
        return new FloatGaugeReading(name, unit, value);
    }
    
    public FloatGaugeReading createDoubleGaugeReading(String name, String unit, Float value, Float warning, Float critical, Float min, Float max)
    {
        return new FloatGaugeReading(name, unit, value, warning, critical, min, max);
    }
}

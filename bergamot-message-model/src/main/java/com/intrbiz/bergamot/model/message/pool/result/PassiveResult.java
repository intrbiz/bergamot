package com.intrbiz.bergamot.model.message.pool.result;

import java.util.UUID;
import java.util.function.BiPredicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchOn;
import com.intrbiz.bergamot.model.message.pool.result.match.MatchableMO;

/**
 * The result of a passive check
 */
@JsonTypeName("bergamot.result.passive")
public class PassiveResult extends ResultMessage implements MatchableMO
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("match_on")
    private MatchOn matchOn;

    public PassiveResult()
    {
        super();
    }

    @Override
    public MatchOn getMatchOn()
    {
        return matchOn;
    }

    @Override
    public void setMatchOn(MatchOn matchOn)
    {
        this.matchOn = matchOn;
    }
    
    @Override
    public int getPool()
    {
        return this.matchOn == null ? -1 : this.matchOn.getPool();
    }

    @JsonIgnore
    public PassiveResult passive(UUID siteId, MatchOn matchCriteria)
    {
        this.setSiteId(siteId);
        this.setMatchOn(matchCriteria);
        this.setId(UUID.randomUUID());
        this.setExecuted(System.currentTimeMillis());
        this.setRuntime(0);
        return this;
    }
    
    @Override
    public PassiveResult runtime(double runtime)
    {
        super.runtime(runtime);
        return this;
    }

    @Override
    public PassiveResult pending(String output)
    {
        super.pending(output);
        return this;
    }
    
    @Override
    public PassiveResult info(String output)
    {
        super.info(output);
        return this;
    }

    @Override
    public PassiveResult ok(String output)
    {
        super.ok(output);
        return this;
    }

    @Override
    public PassiveResult warning(String output)
    {
        super.warning(output);
        return this;
    }

    @Override
    public PassiveResult critical(String output)
    {
        super.critical(output);
        return this;
    }

    @Override
    public PassiveResult unknown(String output)
    {
        super.unknown(output);
        return this;
    }

    @Override
    public PassiveResult error(Throwable t)
    {
        super.error(t);
        return this;
    }

    @Override
    public PassiveResult error(String message)
    {
        super.error(message);
        return this;
    }

    @Override
    public PassiveResult timeout(String message)
    {
        super.timeout(message);
        return this;
    }
    
    @Override
    public PassiveResult disconnected(String message)
    {
        super.disconnected(message);
        return this;
    }
    
    @Override
    public PassiveResult action(String message)
    {
        super.action(message);
        return this;
    }

    @Override
    @JsonIgnore
    public <V,T> PassiveResult applyThreshold(V value, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        super.applyThreshold(value, match, warning, critical, message);
        return this;
    }

    @Override
    @JsonIgnore
    public <V,T> PassiveResult applyThresholds(Iterable<V> values, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        super.applyThresholds(values, match, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThreshold(Double value, Double warning, Double critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThreshold(Double value, Double warning, Double critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThreshold(Float value, Float warning, Float critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThreshold(Float value, Float warning, Float critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThreshold(Long value, Long warning, Long critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThreshold(Long value, Long warning, Long critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyGreaterThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyLessThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public <V,T> PassiveResult applyRange(V value, BiPredicate<V,T> lowerMatch, BiPredicate<V,T> upperMatch, T[] warning, T[] critical, String message)
    {
        super.applyRange(value, lowerMatch, upperMatch, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyRange(Long value, Long[] warning, Long[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyRange(Integer value, Integer[] warning, Integer[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyRange(Double value, Double[] warning, Double[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResult applyRange(Float value, Float[] warning, Float[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
}

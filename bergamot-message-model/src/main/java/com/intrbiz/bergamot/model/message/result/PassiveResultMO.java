package com.intrbiz.bergamot.model.message.result;

import java.util.UUID;
import java.util.function.BiPredicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The result of a passive check
 */
@JsonTypeName("bergamot.result.passive")
public class PassiveResultMO extends ResultMO
{
    @JsonProperty("site_id")
    private UUID siteId;

    @JsonProperty("match_on")
    private MatchOn matchOn;

    public PassiveResultMO()
    {
        super();
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public MatchOn getMatchOn()
    {
        return matchOn;
    }

    public void setMatchOn(MatchOn matchOn)
    {
        this.matchOn = matchOn;
    }

    @JsonIgnore
    public PassiveResultMO passive(UUID siteId, MatchOn matchCriteria)
    {
        this.setSiteId(siteId);
        this.setMatchOn(matchCriteria);
        this.setId(UUID.randomUUID());
        this.setExecuted(System.currentTimeMillis());
        this.setRuntime(0);
        return this;
    }
    
    @Override
    public PassiveResultMO runtime(double runtime)
    {
        super.runtime(runtime);
        return this;
    }

    @Override
    public PassiveResultMO pending(String output)
    {
        super.pending(output);
        return this;
    }
    
    @Override
    public PassiveResultMO info(String output)
    {
        super.info(output);
        return this;
    }

    @Override
    public PassiveResultMO ok(String output)
    {
        super.ok(output);
        return this;
    }

    @Override
    public PassiveResultMO warning(String output)
    {
        super.warning(output);
        return this;
    }

    @Override
    public PassiveResultMO critical(String output)
    {
        super.critical(output);
        return this;
    }

    @Override
    public PassiveResultMO unknown(String output)
    {
        super.unknown(output);
        return this;
    }

    @Override
    public PassiveResultMO error(Throwable t)
    {
        super.error(t);
        return this;
    }

    @Override
    public PassiveResultMO error(String message)
    {
        super.error(message);
        return this;
    }

    @Override
    public PassiveResultMO timeout(String message)
    {
        super.timeout(message);
        return this;
    }
    
    @Override
    public PassiveResultMO disconnected(String message)
    {
        super.disconnected(message);
        return this;
    }
    
    @Override
    public PassiveResultMO action(String message)
    {
        super.action(message);
        return this;
    }

    @Override
    @JsonIgnore
    public <V,T> PassiveResultMO applyThreshold(V value, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        super.applyThreshold(value, match, warning, critical, message);
        return this;
    }

    @Override
    @JsonIgnore
    public <V,T> PassiveResultMO applyThresholds(Iterable<V> values, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        super.applyThresholds(values, match, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThreshold(Double value, Double warning, Double critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThreshold(Double value, Double warning, Double critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThreshold(Float value, Float warning, Float critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThreshold(Float value, Float warning, Float critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThreshold(Long value, Long warning, Long critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThreshold(Long value, Long warning, Long critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyGreaterThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyLessThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public <V,T> PassiveResultMO applyRange(V value, BiPredicate<V,T> lowerMatch, BiPredicate<V,T> upperMatch, T[] warning, T[] critical, String message)
    {
        super.applyRange(value, lowerMatch, upperMatch, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyRange(Long value, Long[] warning, Long[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyRange(Integer value, Integer[] warning, Integer[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyRange(Double value, Double[] warning, Double[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public PassiveResultMO applyRange(Float value, Float[] warning, Float[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
}

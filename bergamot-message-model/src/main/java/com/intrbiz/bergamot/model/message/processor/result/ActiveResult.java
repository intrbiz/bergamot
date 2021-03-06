package com.intrbiz.bergamot.model.message.processor.result;

import java.util.UUID;
import java.util.function.BiPredicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

/**
 * The result of a active check
 */
@JsonTypeName("bergamot.processor.result.active")
public class ActiveResult extends ResultMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("check_type")
    private String checkType;

    @JsonProperty("check_id")
    private UUID checkId;
    
    @JsonProperty("saved_state")
    private String savedState;
    
    @JsonProperty("scheduled")
    private long scheduled;
    
    @JsonProperty("received")
    private long received;

    public ActiveResult()
    {
        super();
    }
    
    public String getCheckType()
    {
        return checkType;
    }

    public void setCheckType(String checkType)
    {
        this.checkType = checkType;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public String getSavedState()
    {
        return savedState;
    }

    public void setSavedState(String savedState)
    {
        this.savedState = savedState;
    }

    public long getReceived()
    {
        return this.received;
    }

    public void setReceived(long received)
    {
        this.received = received;
    }

    public long getScheduled()
    {
        return this.scheduled;
    }

    public void setScheduled(long scheduled)
    {
        this.scheduled = scheduled;
    }

    /**
     * Create a Result with the details of this check
     * 
     * @return
     */
    @JsonIgnore
    public ActiveResult _fromCheck(ExecuteCheck check)
    {
        this.setId(check.getId());
        this.setCheckType(check.getCheckType());
        this.setCheckId(check.getCheckId());
        this.setSiteId(check.getSiteId());
        this.setScheduled(check.getScheduled());
        this.setReceived(check.getReceived());
        this.setAdhocId(check.getAdhocId());
        return this;
    }
    
    @JsonIgnore
    public ActiveResult withProcessorId(UUID processorId)
    {
        this.setProcessorId(processorId);
        return this;
    }
    
    public ActiveResult state(String savedState)
    {
        this.savedState = savedState;
        return this;
    }

    @Override
    public ActiveResult pending(String output)
    {
        super.pending(output);
        return this;
    }
    
    @Override
    public ActiveResult info(String output)
    {
        super.info(output);
        return this;
    }

    @Override
    public ActiveResult ok(String output)
    {
        super.ok(output);
        return this;
    }

    @Override
    public ActiveResult warning(String output)
    {
        super.warning(output);
        return this;
    }

    @Override
    public ActiveResult critical(String output)
    {
        super.critical(output);
        return this;
    }

    @Override
    public ActiveResult unknown(String output)
    {
        super.unknown(output);
        return this;
    }

    @Override
    public ActiveResult error(Throwable t)
    {
        super.error(t);
        return this;
    }

    @Override
    public ActiveResult error(String message)
    {
        super.error(message);
        return this;
    }

    @Override
    public ActiveResult timeout(String message)
    {
        super.timeout(message);
        return this;
    }
    
    @Override
    public ActiveResult disconnected(String message)
    {
        super.disconnected(message);
        return this;
    }
    
    @Override
    public ActiveResult action(String message)
    {
        super.action(message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public <V,T> ActiveResult applyThreshold(V value, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        super.applyThreshold(value, match, warning, critical, message);
        return this;
    }

    @Override
    @JsonIgnore
    public <V,T> ActiveResult applyThresholds(Iterable<V> values, BiPredicate<V,T> match, T warning, T critical, String message)
    {
        super.applyThresholds(values, match, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThreshold(Double value, Double warning, Double critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThreshold(Double value, Double warning, Double critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThreshold(Float value, Float warning, Float critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThreshold(Float value, Float warning, Float critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThreshold(Long value, Long warning, Long critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThreshold(Long value, Long warning, Long critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThreshold(Integer value, Integer warning, Integer critical, String message)
    {
        super.applyThreshold(value, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThresholds(Iterable<Double> values, Double warning, Double critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThresholds(Iterable<Float> values, Float warning, Float critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThresholds(Iterable<Long> values, Long warning, Long critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyGreaterThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyLessThanThresholds(Iterable<Integer> values, Integer warning, Integer critical, String message)
    {
        super.applyThresholds(values, (v,t) -> v < t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public <V,T> ActiveResult applyRange(V value, BiPredicate<V,T> lowerMatch, BiPredicate<V,T> upperMatch, T[] warning, T[] critical, String message)
    {
        super.applyRange(value, lowerMatch, upperMatch, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyRange(Long value, Long[] warning, Long[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyRange(Integer value, Integer[] warning, Integer[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyRange(Double value, Double[] warning, Double[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
    
    @Override
    @JsonIgnore
    public ActiveResult applyRange(Float value, Float[] warning, Float[] critical, String message)
    {
        super.applyRange(value, (v,t) -> v < t, (v,t) -> v > t, warning, critical, message);
        return this;
    }
}

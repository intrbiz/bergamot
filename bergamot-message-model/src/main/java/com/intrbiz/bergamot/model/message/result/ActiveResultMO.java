package com.intrbiz.bergamot.model.message.result;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;

/**
 * The result of a active check
 */
@JsonTypeName("bergamot.result.active")
public class ActiveResultMO extends ResultMO
{
    @JsonProperty("check_type")
    private String checkType;

    @JsonProperty("check_id")
    private UUID checkId;

    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("processing_pool")
    private int processingPool;

    @JsonProperty("check")
    private CheckEvent check;

    public ActiveResultMO()
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

    public CheckEvent getCheck()
    {
        return check;
    }

    public void setCheck(CheckEvent check)
    {
        this.check = check;
    }
    
    
    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public int getProcessingPool()
    {
        return processingPool;
    }

    public void setProcessingPool(int processingPool)
    {
        this.processingPool = processingPool;
    }
    

    /**
     * Create a Result with the details of this check
     * 
     * @return
     */
    @JsonIgnore
    public ActiveResultMO fromCheck(CheckEvent check)
    {
        this.setId(check.getId());
        this.setCheckType(check.getCheckType());
        this.setCheckId(check.getCheckId());
        this.setSiteId(check.getSiteId());
        this.setProcessingPool(check.getProcessingPool());
        this.setCheck(check);
        this.setExecuted(System.currentTimeMillis());
        return this;
    }

    @Override
    public ActiveResultMO pending(String output)
    {
        return (ActiveResultMO) super.pending(output);
    }

    @Override
    public ActiveResultMO ok(String output)
    {
        return (ActiveResultMO) super.ok(output);
    }

    @Override
    public ActiveResultMO warning(String output)
    {
        return (ActiveResultMO) super.warning(output);
    }

    @Override
    public ActiveResultMO critical(String output)
    {
        return (ActiveResultMO) super.critical(output);
    }

    @Override
    public ActiveResultMO unknown(String output)
    {
        return (ActiveResultMO) super.unknown(output);
    }

    @Override
    public ActiveResultMO error(Throwable t)
    {
        return (ActiveResultMO) super.error(t);
    }

    @Override
    public ActiveResultMO error(String message)
    {
        return (ActiveResultMO) super.error(message);
    }

    @Override
    public ActiveResultMO timeout(String message)
    {
        return (ActiveResultMO) super.timeout(message);
    }

    @Override
    public ActiveResultMO applyThreshold(double value, double warning, double critical, String message)
    {
        return (ActiveResultMO) super.applyThreshold(value, warning, critical, message);
    }

    @Override
    public ActiveResultMO applyThreshold(long value, long warning, long critical, String message)
    {
        return (ActiveResultMO) super.applyThreshold(value, warning, critical, message);
    }
    
    @Override
    public ActiveResultMO applyThreshold(Iterable<Double> values, double warning, double critical, String message)
    {
        return (ActiveResultMO) super.applyThreshold(values, warning, critical, message);
    }
    
    @Override
    public ActiveResultMO applyThreshold(Iterable<Long> values, long warning, long critical, String message)
    {
        return (ActiveResultMO) super.applyThreshold(values, warning, critical, message);
    }

    @Override
    public ActiveResultMO runtime(double runtime)
    {
        return (ActiveResultMO) super.runtime(runtime);
    }
}

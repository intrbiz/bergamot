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
    public ActiveResultMO runtime(double runtime)
    {
        super.runtime(runtime);
        return this;
    }

    @Override
    public ActiveResultMO pending(String output)
    {
        super.pending(output);
        return this;
    }
    
    @Override
    public ActiveResultMO info(String output)
    {
        super.info(output);
        return this;
    }

    @Override
    public ActiveResultMO ok(String output)
    {
        super.ok(output);
        return this;
    }

    @Override
    public ActiveResultMO warning(String output)
    {
        super.warning(output);
        return this;
    }

    @Override
    public ActiveResultMO critical(String output)
    {
        super.critical(output);
        return this;
    }

    @Override
    public ActiveResultMO unknown(String output)
    {
        super.unknown(output);
        return this;
    }

    @Override
    public ActiveResultMO error(Throwable t)
    {
        super.error(t);
        return this;
    }

    @Override
    public ActiveResultMO error(String message)
    {
        super.error(message);
        return this;
    }

    @Override
    public ActiveResultMO timeout(String message)
    {
        super.timeout(message);
        return this;
    }
    
    @Override
    public ActiveResultMO disconnected(String message)
    {
        super.disconnected(message);
        return this;
    }
    
    @Override
    public ActiveResultMO action(String message)
    {
        super.action(message);
        return this;
    }
}

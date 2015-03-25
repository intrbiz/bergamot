package com.intrbiz.bergamot.model.message.result;

import java.util.UUID;

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
    public PassiveResultMO applyThreshold(double value, double warning, double critical, String message)
    {
        super.applyThreshold(value, warning, critical, message);
        return this;
    }

    @Override
    public PassiveResultMO applyThreshold(long value, long warning, long critical, String message)
    {
        super.applyThreshold(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyThreshold(Iterable<Double> values, double warning, double critical, String message)
    {
        super.applyThreshold(values, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyThreshold(Iterable<Long> values, long warning, long critical, String message)
    {
        super.applyThreshold(values, warning, critical, message);
        return this;
    }

    @Override
    public PassiveResultMO runtime(double runtime)
    {
        super.runtime(runtime);
        return this;
    }
    
    @Override
    public PassiveResultMO applyRange(int value, int[] warning, int[] critical, String message)
    {
        super.applyRange(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyRange(long value, long[] warning, long[] critical, String message)
    {
        super.applyRange(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyRange(float value, float[] warning, float[] critical, String message)
    {
        super.applyRange(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyRange(double value, double[] warning, double[] critical, String message)
    {
        super.applyRange(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyLessThanThreshold(double value, double warning, double critical, String message)
    {
        super.applyLessThanThreshold(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyLessThanThreshold(long value, long warning, long critical, String message)
    {
        super.applyLessThanThreshold(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyLessThanThreshold(Iterable<Double> value, double warning, double critical, String message)
    {
        super.applyLessThanThreshold(value, warning, critical, message);
        return this;
    }
    
    @Override
    public PassiveResultMO applyLessThanThreshold(Iterable<Long> value, long warning, long critical, String message)
    {
        super.applyLessThanThreshold(value, warning, critical, message);
        return this;
    }
}

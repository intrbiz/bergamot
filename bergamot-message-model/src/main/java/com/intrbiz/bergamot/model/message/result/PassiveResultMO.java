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
        return (PassiveResultMO) super.pending(output);
    }

    @Override
    public PassiveResultMO ok(String output)
    {
        return (PassiveResultMO) super.ok(output);
    }

    @Override
    public PassiveResultMO warning(String output)
    {
        return (PassiveResultMO) super.warning(output);
    }

    @Override
    public PassiveResultMO critical(String output)
    {
        return (PassiveResultMO) super.critical(output);
    }

    @Override
    public PassiveResultMO unknown(String output)
    {
        return (PassiveResultMO) super.unknown(output);
    }

    @Override
    public PassiveResultMO error(Throwable t)
    {
        return (PassiveResultMO) super.error(t);
    }

    @Override
    public PassiveResultMO error(String message)
    {
        return (PassiveResultMO) super.error(message);
    }

    @Override
    public PassiveResultMO timeout(String message)
    {
        return (PassiveResultMO) super.timeout(message);
    }

    @Override
    public PassiveResultMO applyThreshold(double value, double warning, double critical, String message)
    {
        return (PassiveResultMO) super.applyThreshold(value, warning, critical, message);
    }

    @Override
    public PassiveResultMO applyThreshold(long value, long warning, long critical, String message)
    {
        return (PassiveResultMO) super.applyThreshold(value, warning, critical, message);
    }

    @Override
    public PassiveResultMO runtime(double runtime)
    {
        return (PassiveResultMO) super.runtime(runtime);
    }
}

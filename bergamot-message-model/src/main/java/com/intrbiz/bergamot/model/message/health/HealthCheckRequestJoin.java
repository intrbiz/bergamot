package com.intrbiz.bergamot.model.message.health;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Request that all daemons send join messages
 */
@JsonTypeName("bergamot.healthcheck.request-join")
public class HealthCheckRequestJoin extends HealthCheckMessage
{    
    public HealthCheckRequestJoin()
    {
        super();
    }
}

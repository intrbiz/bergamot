package com.intrbiz.bergamot.model.message.event.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.LocationMO;

/**
 * A location state update
 */
@JsonTypeName("bergamot.location_update")
public class LocationUpdate extends Update
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("location")
    private LocationMO location;

    public LocationUpdate()
    {
        super();
    }
    
    public LocationUpdate(LocationMO location)
    {
        super(System.currentTimeMillis());
        this.location = location;
    }

    public LocationMO getLocation()
    {
        return location;
    }

    public void setLocation(LocationMO location)
    {
        this.location = location;
    }
}

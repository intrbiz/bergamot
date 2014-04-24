package com.intrbiz.bergamot.compat.config.model;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;

@TypeName("location")
public class LocationCfg extends ConfigObject<LocationCfg>
{
    private String locationName;
    
    private String location;

    private String alias;

    public LocationCfg()
    {
    }

    public String getLocationName()
    {
        return locationName;
    }

    @ParameterName("location_name")
    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public String getAlias()
    {
        return alias;
    }

    @ParameterName("alias")
    public void setAlias(String alias)
    {
        this.alias = alias;
    }
    
    public String getLocation()
    {
        return location;
    }

    @ParameterName("location")
    public void setLocation(String location)
    {
        this.location = location;
    }
    
    public String resolveAlias()
    {
        return this.resolveProperty((p) -> { return p.getAlias(); });
    }

    public String resolveLocationName()
    {
        return this.resolveProperty((p) -> { return p.getLocationName(); });
    }
    
    public String resolveLocation()
    {
        return this.resolveProperty((p) -> { return p.getLocation(); });
    }
    
    public String toString()
    {
        return "location { " + this.locationName + " }";
    }
}

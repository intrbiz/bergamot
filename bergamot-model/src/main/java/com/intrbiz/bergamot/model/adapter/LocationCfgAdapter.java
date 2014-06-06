package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class LocationCfgAdapter implements DBTypeAdapter<String, LocationCfg>
{    
    @Override
    public String toDB(LocationCfg value)
    {
        return value == null ? null : Configuration.toString(LocationCfg.class, value);
    }

    @Override
    public LocationCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(LocationCfg.class, value);
    }
}

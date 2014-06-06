package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class ResourceCfgAdapter implements DBTypeAdapter<String, ResourceCfg>
{    
    @Override
    public String toDB(ResourceCfg value)
    {
        return value == null ? null : Configuration.toString(ResourceCfg.class, value);
    }

    @Override
    public ResourceCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(ResourceCfg.class, value);
    }
}

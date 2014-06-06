package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class HostCfgAdapter implements DBTypeAdapter<String, HostCfg>
{    
    @Override
    public String toDB(HostCfg value)
    {
        return value == null ? null : Configuration.toString(HostCfg.class, value);
    }

    @Override
    public HostCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(HostCfg.class, value);
    }
}

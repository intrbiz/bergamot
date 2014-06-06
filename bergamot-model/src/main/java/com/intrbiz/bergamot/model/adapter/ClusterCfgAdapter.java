package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class ClusterCfgAdapter implements DBTypeAdapter<String, ClusterCfg>
{    
    @Override
    public String toDB(ClusterCfg value)
    {
        return value == null ? null : Configuration.toString(ClusterCfg.class, value);
    }

    @Override
    public ClusterCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(ClusterCfg.class, value);
    }
}

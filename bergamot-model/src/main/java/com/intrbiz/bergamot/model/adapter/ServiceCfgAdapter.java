package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class ServiceCfgAdapter implements DBTypeAdapter<String, ServiceCfg>
{    
    @Override
    public String toDB(ServiceCfg value)
    {
        return value == null ? null : Configuration.toString(ServiceCfg.class, value);
    }

    @Override
    public ServiceCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(ServiceCfg.class, value);
    }
}

package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class TimePeriodCfgAdapter implements DBTypeAdapter<String, TimePeriodCfg>
{    
    @Override
    public String toDB(TimePeriodCfg value)
    {
        return value == null ? null : Configuration.toString(TimePeriodCfg.class, value);
    }

    @Override
    public TimePeriodCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(TimePeriodCfg.class, value);
    }
}

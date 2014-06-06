package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class TrapCfgAdapter implements DBTypeAdapter<String, TrapCfg>
{    
    @Override
    public String toDB(TrapCfg value)
    {
        return value == null ? null : Configuration.toString(TrapCfg.class, value);
    }

    @Override
    public TrapCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(TrapCfg.class, value);
    }
}

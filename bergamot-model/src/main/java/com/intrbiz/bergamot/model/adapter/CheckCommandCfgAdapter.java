package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.CheckCommandCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class CheckCommandCfgAdapter implements DBTypeAdapter<String, CheckCommandCfg>
{    
    @Override
    public String toDB(CheckCommandCfg value)
    {
        return value == null ? null : Configuration.toString(CheckCommandCfg.class, value);
    }

    @Override
    public CheckCommandCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(CheckCommandCfg.class, value);
    }
}

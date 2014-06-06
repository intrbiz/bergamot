package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class BergamotCfgAdapter implements DBTypeAdapter<String, Configuration>
{    
    @Override
    public String toDB(Configuration value)
    {
        return value == null ? null : Configuration.toString(new Class<?>[] { BergamotCfg.class }, value);
    }

    @Override
    public Configuration fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(new Class<?>[] { BergamotCfg.class }, value);
    }
}

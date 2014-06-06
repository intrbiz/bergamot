package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class CommandCfgAdapter implements DBTypeAdapter<String, CommandCfg>
{    
    @Override
    public String toDB(CommandCfg value)
    {
        return value == null ? null : Configuration.toString(CommandCfg.class, value);
    }

    @Override
    public CommandCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(CommandCfg.class, value);
    }
}

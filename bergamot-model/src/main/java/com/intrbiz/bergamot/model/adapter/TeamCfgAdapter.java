package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class TeamCfgAdapter implements DBTypeAdapter<String, TeamCfg>
{    
    @Override
    public String toDB(TeamCfg value)
    {
        return value == null ? null : Configuration.toString(TeamCfg.class, value);
    }

    @Override
    public TeamCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(TeamCfg.class, value);
    }
}

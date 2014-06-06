package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class GroupCfgAdapter implements DBTypeAdapter<String, GroupCfg>
{    
    @Override
    public String toDB(GroupCfg value)
    {
        return value == null ? null : Configuration.toString(GroupCfg.class, value);
    }

    @Override
    public GroupCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(GroupCfg.class, value);
    }
}

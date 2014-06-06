package com.intrbiz.bergamot.model.adapter;

import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class ContactCfgAdapter implements DBTypeAdapter<String, ContactCfg>
{    
    @Override
    public String toDB(ContactCfg value)
    {
        return value == null ? null : Configuration.toString(ContactCfg.class, value);
    }

    @Override
    public ContactCfg fromDB(String value)
    {
        return value == null ? null : Configuration.fromString(ContactCfg.class, value);
    }
}

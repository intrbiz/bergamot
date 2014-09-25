package com.intrbiz.bergamot.data;

import java.util.UUID;

import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.configuration.Configuration;

public class ConfigResolver extends AbstractConfigResolver
{   
    private UUID siteId;
    
    private BergamotDB db;
    
    public ConfigResolver(BergamotDB db, UUID siteId)
    {
        super();
        this.db = db;
        this.siteId = siteId;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
    {
        Config cfg = db.getConfigByName(this.siteId, Configuration.getRootElement(type), name);
        return cfg == null ? null : (T) cfg.getConfiguration();
    }
}

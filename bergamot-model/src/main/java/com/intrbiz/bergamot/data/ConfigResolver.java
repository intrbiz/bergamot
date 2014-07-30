package com.intrbiz.bergamot.data;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.configuration.Configuration;

public class ConfigResolver
{   
    private Logger logger = Logger.getLogger(ConfigResolver.class);
    
    private UUID siteId;
    
    private BergamotDB db;
    
    public ConfigResolver(BergamotDB db, UUID siteId)
    {
        super();
        this.db = db;
        this.siteId = siteId;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends TemplatedObjectCfg<T>> T resolveInherit(T object)
    {
        for (String inheritsFrom : object.getInheritedTemplates())
        {
            TemplatedObjectCfg<?> superObject = lookup(object.getClass(), inheritsFrom);
            if (superObject != null)
            {
                ((TemplatedObjectCfg) object).addInheritedObject(superObject);
            }
            else
            {
                // error
                logger.warn("Error: Cannot find the inherited " + object.getClass().getSimpleName() + " named '" + inheritsFrom + "' which is inherited by " + object);
            }
        }
        if (object instanceof TimePeriodCfg)
        {
            resolveExcludes((TimePeriodCfg) object);
        }
        return object;
    }
    
    private TimePeriodCfg resolveExcludes(TimePeriodCfg object)
    {
        for (String exclude : object.getExcludes())
        {
            TimePeriodCfg excludedTimePeriod = lookup(TimePeriodCfg.class, exclude);
            if (excludedTimePeriod == null)
            {
                // error
                logger.warn("Error: Cannot find the excluded time period named '" + exclude + "' which is excluded by " + object);
            }
        }
        return object;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
    {
        Config cfg = db.getConfigByName(this.siteId, Configuration.getRootElement(type), name);
        return cfg == null ? null : (T) cfg.getConfiguration();
    }
}

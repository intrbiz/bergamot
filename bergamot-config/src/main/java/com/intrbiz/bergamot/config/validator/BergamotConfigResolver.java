package com.intrbiz.bergamot.config.validator;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;

public class BergamotConfigResolver
{   
    private Logger logger = Logger.getLogger(BergamotConfigResolver.class);
    
    protected final BergamotObjectLocator locator;
    
    public BergamotConfigResolver(BergamotObjectLocator locator)
    {
        super();
        this.locator = locator;
    }
    
    public BergamotObjectLocator getLocator()
    {
        return this.locator;
    }
    
    public <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
    {
        return this.locator.lookup(type, name);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends TemplatedObjectCfg<T>> T resolveInherit(T object)
    {
        for (String inheritsFrom : object.getInheritedTemplates())
        {
            TemplatedObjectCfg<?> superObject = this.lookup(object.getClass(), inheritsFrom);
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
            TimePeriodCfg excludedTimePeriod = this.lookup(TimePeriodCfg.class, exclude);
            if (excludedTimePeriod == null)
            {
                // error
                logger.warn("Error: Cannot find the excluded time period named '" + exclude + "' which is excluded by " + object);
            }
        }
        return object;
    }
}

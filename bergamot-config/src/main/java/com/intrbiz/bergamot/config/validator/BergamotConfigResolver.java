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
        T inherited = this.locator.lookup(type, name);
        logger.debug("Looked up inherited object: " + type.getSimpleName() + "::" + name + " ==> " + inherited);
        return inherited;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T extends TemplatedObjectCfg<T>> T resolveInherit(T object)
    {
        if (((TemplatedObjectCfg) object).getInherits().size() > 0)
        {
            logger.debug("Skipping previously resolved object: " + object.getClass().getSimpleName() + "::" + object.getName());
        }
        else
        {
            logger.debug("Resolving inheritance for " + object.getClass().getSimpleName() + "::" + object.getName());
            for (String inheritsFrom : object.getInheritedTemplates())
            {
                logger.debug("Looking up inherited object: " + inheritsFrom);   
                TemplatedObjectCfg<?> superObject = this.lookup(object.getClass(), inheritsFrom);
                if (superObject != null)
                {
                    // we need to recursively ensure that the inherited object is resolved
                    this.resolveInherit((TemplatedObjectCfg) superObject);
                    // add the inherited object
                    ((TemplatedObjectCfg) object).addInheritedObject(superObject);
                }
                else
                {
                    // error
                    logger.error("Cannot find the inherited object " + inheritsFrom + "!");
                    logger.warn("Error: Cannot find the inherited " + object.getClass().getSimpleName() + " named '" + inheritsFrom + "' which is inherited by " + object);
                }
            }
            // child objects
            for (TemplatedObjectCfg<?> child : object.getTemplatedChildObjects())
            {
                this.resolveInherit((TemplatedObjectCfg) child);
            }
            // special cases
            if (object instanceof TimePeriodCfg)
            {
                resolveExcludes((TimePeriodCfg) object);
            }
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

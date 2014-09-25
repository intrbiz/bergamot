package com.intrbiz.bergamot.config.validator;

import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;

public interface BergamotObjectLocator
{
    <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name);
    
    /**
     * Create a locator which will lookup objects from multiple sources
     */
    public static BergamotObjectLocator from(final BergamotObjectLocator locator, final BergamotObjectLocator... locators)
    {
        return new BergamotObjectLocator()
        {
            @Override
            public <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
            {
                T ret = locator.lookup(type, name);
                if (ret == null)
                {
                    for (BergamotObjectLocator locator : locators)
                    {
                        ret = locator.lookup(type, name);
                        if (ret != null) return ret;
                    }
                }
                return ret;
            }           
        };
    }
}

package com.intrbiz.bergamot.config.resolver.stratergy;

import java.util.Collection;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class CoalesceEmptyCollection implements ObjectResolver<Collection<?>>
{
    public Collection<?> resolve(Collection<?> m, Collection<?> l)
    {
        return (m != null && (! m.isEmpty())) ? m : l;
    }

}

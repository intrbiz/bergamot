package com.intrbiz.bergamot.config.resolver.stratergy;

import java.util.LinkedHashSet;
import java.util.Set;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class MergeSet<T> implements ObjectResolver<Set<T>>
{
    public Set<T> resolve(Set<T> m, Set<T> l)
    {
        Set<T> r = new LinkedHashSet<T>();
        if (l != null)
        {
            for (T e : l)
            {
                r.add(e);
            }
        }
        if (m != null)
        {
            for (T e : m)
            {
                r.add(e);
            }
        }
        return r;
    }
}

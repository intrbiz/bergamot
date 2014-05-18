package com.intrbiz.bergamot.config.resolver.stratergy;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class MergeListUnique<T> implements ObjectResolver<List<T>>
{
    public List<T> resolve(List<T> m, List<T> l)
    {
        List<T> r = new LinkedList<T>();
        if (m != null)
        {
            for (T e : m)
            {
                if (! r.contains(e)) r.add(e);
            }
        }
        if (l != null)
        {
            for (T e : l)
            {
                if (! r.contains(e)) r.add(e);
            }
        }
        return r;
    }
}

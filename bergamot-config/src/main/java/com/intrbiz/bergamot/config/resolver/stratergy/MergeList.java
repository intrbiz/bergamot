package com.intrbiz.bergamot.config.resolver.stratergy;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class MergeList<T> implements ObjectResolver<List<T>>
{
    public List<T> resolve(List<T> m, List<T> l)
    {
        List<T> r = new LinkedList<T>();
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

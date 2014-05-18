package com.intrbiz.bergamot.config.resolver.stratergy;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class Coalesce<T> implements ObjectResolver<T>
{
    public T resolve(T m, T l)
    {
        return m != null ? m : l;
    }
}

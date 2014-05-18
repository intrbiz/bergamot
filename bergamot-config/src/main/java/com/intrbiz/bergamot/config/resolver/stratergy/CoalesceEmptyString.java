package com.intrbiz.bergamot.config.resolver.stratergy;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class CoalesceEmptyString implements ObjectResolver<String>
{
    public String resolve(String m, String l)
    {
        return (m != null && m.length() > 0) ? m : l;
    }
}

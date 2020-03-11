package com.intrbiz.bergamot.agent.util;

public final class AgentUtil
{
    public static final boolean isEmpty(String s)
    {
        return s == null || s.trim().length() == 0;
    }
    
    public static final String coalesce(String... ss)
    {
        for (String s : ss)
        {
            if (! isEmpty(s))
                return s;
        }
        return null;
    }
}

package com.intrbiz.bergamot.config.resolver.stratergy;

import java.util.LinkedHashSet;
import java.util.Set;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class SmartMergeSet implements ObjectResolver<Set<String>>
{
    public Set<String> finish(Set<String> resolved)
    {
        if (resolved == null || resolved.isEmpty()) return resolved;
        // stip off any merge prefixes
        Set<String> finished = new LinkedHashSet<String>();
        for (String item : resolved)
        {
            if (item != null && item.length() > 0)
            {
                if (item.startsWith("-") || item.startsWith("+"))
                {
                    finished.add(item.substring(1));
                }
                else
                {
                    finished.add(item);
                }
            }
        }
        return finished;
    }
    
    public Set<String> resolve(Set<String> m, Set<String> l)
    {
        Set<String> r = new LinkedHashSet<String>();
        if (l != null)
        {
            for (String e : l)
            {
                this.merge(e, r);
            }
        }
        if (m != null)
        {
            for (String e : m)
            {
                this.merge(e, r);
            }
        }
        return r;
    }
    
    private void merge(String e, Set<String> c)
    {
        if (e != null && e.length() > 0)
        {
            if (e.startsWith("-"))
            {
                c.remove(e.substring(1));
            }
            else if (e.startsWith("+"))
            {
                String s = e.substring(1);
                c.add(s);
            }
            else
            {
                c.add(e);
            }
        }
    }
}

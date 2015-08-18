package com.intrbiz.bergamot.config.resolver.stratergy;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.config.resolver.ObjectResolver;

public class SmartMergeList implements ObjectResolver<List<String>>
{
    public List<String> resolve(List<String> m, List<String> l)
    {
        List<String> r = new LinkedList<String>();
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
    
    private void merge(String e, List<String> c)
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
                if (! c.contains(s)) c.add(s);
            }
            else
            {
                if (! c.contains(e)) c.add(e);
            }
        }
    }
}

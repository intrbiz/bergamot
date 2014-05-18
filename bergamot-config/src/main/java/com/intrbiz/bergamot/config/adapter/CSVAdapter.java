package com.intrbiz.bergamot.config.adapter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.intrbiz.Util;

public class CSVAdapter extends XmlAdapter<String, Set<String>>
{
    @Override
    public String marshal(Set<String> arg0) throws Exception
    {
        if (arg0 == null || arg0.isEmpty()) return null;
        return Util.join(", ", arg0);
    }

    @Override
    public Set<String> unmarshal(String arg0) throws Exception
    {
        if (arg0 == null) return new LinkedHashSet<String>();
        Set<String> r = new LinkedHashSet<String>();
        for (String s : arg0.split(", ?"))
        {
            s = s.trim();
            if (! Util.isEmpty(s))
            {
                r.add(s);
            }
        }
        return r;
    }
}

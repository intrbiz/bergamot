package com.intrbiz.bergamot.config.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class YesNoAdapter extends XmlAdapter<String, Boolean>
{   
    @Override
    public String marshal(Boolean arg0) throws Exception
    {
        if (arg0 == null) return null;
        return  arg0.booleanValue() ? "yes" : "no";
    }

    @Override
    public Boolean unmarshal(String arg0) throws Exception
    {
        if (arg0 == null) return null;
        if (arg0.equalsIgnoreCase("yes") || arg0.equalsIgnoreCase("true") || arg0.equalsIgnoreCase("1")) return Boolean.TRUE;
        return Boolean.FALSE;
    }
}

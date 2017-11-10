package com.intrbiz.bergamot.config.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date>
{
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public synchronized String marshal(Date v) throws Exception
    {
        return dateFormat.format(v);
    }

    @Override
    public synchronized Date unmarshal(String v) throws Exception
    {
        return dateFormat.parse(v);
    }
}

package com.intrbiz.bergamot.config.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.bergamot.timerange.TimeRangeParser;

public class TimeRangeAdapter extends XmlAdapter<String, TimeRange>
{
    @Override
    public String marshal(TimeRange arg0) throws Exception
    {
        if (arg0 == null) return null;
        return arg0.toString();
    }

    @Override
    public TimeRange unmarshal(String arg0) throws Exception
    {
        if (Util.isEmpty(arg0)) return null;
        return TimeRangeParser.parseTimeRange(arg0);
    }
}

package com.intrbiz.bergamot.model.adapter;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.bergamot.timerange.TimeRangeParser;
import com.intrbiz.data.db.util.DBTypeAdapter;

public class TimeRangesAdapter implements DBTypeAdapter<List<String>, List<TimeRange>>
{    
    @Override
    public List<String> toDB(List<TimeRange> value)
    {
        return value == null ? null : value.stream().map(TimeRange::toString).collect(Collectors.toList());
    }

    @Override
    public List<TimeRange> fromDB(List<String> value)
    {
        return value == null ? null : value.stream().map(TimeRangeParser::parseTimeRange).collect(Collectors.toList());
    }
}

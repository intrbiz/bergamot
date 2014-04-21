package com.intrbiz.bergamot.timerange;

import java.io.StringReader;

import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.bergamot.timerange.parser.TimeRangeParserInternal;

public class TimeRangeParser
{
    public static TimeRange parseTimeRange(String timerange)
    {
        try
        {
            try (StringReader reader = new StringReader(timerange))
            {
                TimeRangeParserInternal parser = new TimeRangeParserInternal(reader);
                return parser.readTimeRange();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse timerange: '" + timerange + "'", e);
        }
    }
}

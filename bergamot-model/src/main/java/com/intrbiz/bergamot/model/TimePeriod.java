package com.intrbiz.bergamot.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.TimeRangesAdapter;
import com.intrbiz.bergamot.model.message.TimePeriodMO;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A calendar of when checks should be executed
 */
@SQLTable(schema = BergamotDB.class, name = "timeperiod", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class TimePeriod extends NamedObject<TimePeriodMO, TimePeriodCfg> implements TimeRange
{
    private static final long serialVersionUID = 1L;
    
    private transient Logger logger = Logger.getLogger(TimePeriod.class);

    @SQLColumn(index = 1, name = "excludes_id", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<UUID> excludesId = new LinkedList<UUID>();

    @SQLColumn(index = 2, name = "ranges", type = "TEXT[]", adapter = TimeRangesAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    private List<TimeRange> ranges = new LinkedList<TimeRange>();

    public TimePeriod()
    {
        super();
    }

    @Override
    public void configure(TimePeriodCfg cfg)
    {
        super.configure(cfg);
        TimePeriodCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesce(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
        // load the time ranges
        this.ranges.clear();
        this.ranges.addAll(rcfg.getTimeRanges());
    }

    public List<TimeRange> getRanges()
    {
        return ranges;
    }

    public void addRange(TimeRange range)
    {
        this.ranges.add(range);
    }

    public void setRanges(List<TimeRange> ranges)
    {
        this.ranges = ranges;
    }

    public List<TimePeriod> getExcludes()
    {
        List<TimePeriod> r = new LinkedList<TimePeriod>();
        if (this.excludesId != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                for (UUID id : this.getExcludesId())
                {
                    r.add(db.getTimePeriod(id));
                }
            }
        }
        return r;
    }

    public void addExclude(TimePeriod exclude)
    {
        // TODO
    }
    
    public void removeExclude(TimePeriod exclude)
    {
        // TODO
    }

    public List<UUID> getExcludesId()
    {
        return excludesId;
    }

    public void setExcludesId(List<UUID> excludesId)
    {
        this.excludesId = excludesId;
    }

    /**
     * Compute whether the given calendar is within this timeperiod.
     */
    @Override
    public boolean isInTimeRange(Calendar calendar)
    {
        logger.trace("Checking if " + calendar.getTime() + " is valid for this time period");
        // check the possbile excludes
        for (TimePeriod exclude : this.getExcludes())
        {
            if (exclude.isInTimeRange(calendar)) return false;
        }
        // check the ranges
        if (this.ranges.isEmpty()) return true;
        for (TimeRange range : this.getRanges())
        {
            if (range.isInTimeRange(calendar)) return true;
        }
        return false;
    }

    public String toString()
    {
        return "TimePeriod " + this.getName();
    }

    @Override
    public TimePeriodMO toMO(boolean stub)
    {
        TimePeriodMO mo = new TimePeriodMO();
        super.toMO(mo, stub);
        if (!stub)
        {
            mo.setExcludes(this.getExcludes().stream().map(TimePeriod::toStubMO).collect(Collectors.toList()));
            mo.setRanges(this.getRanges().stream().map(TimeRange::toString).collect(Collectors.toList()));
        }
        return mo;
    }

    /*
     * Serialisation shit
     */
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException  
    {  
        is.defaultReadObject();
        this.logger = Logger.getLogger(TimePeriod.class);
    }
}

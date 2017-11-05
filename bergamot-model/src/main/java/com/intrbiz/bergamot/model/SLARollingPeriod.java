package com.intrbiz.bergamot.model;

import java.util.EnumSet;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.SLARollingPeriodCfg;
import com.intrbiz.bergamot.config.model.SLARollingPeriodCfg.RollingPeriodGranularityCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.SLARollingPeriodMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "sla_rolling_period", since = @SQLVersion({ 3, 52, 0 }))
public class SLARollingPeriod extends SLAPeriod<SLARollingPeriodMO>
{
    public enum RollingPeriodGranularity {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 5, name = "granularity", since = @SQLVersion({ 3, 52, 0 }))
    private RollingPeriodGranularity granularity;

    public SLARollingPeriod()
    {
        super();
    }

    public RollingPeriodGranularity getGranularity()
    {
        return granularity;
    }

    public void setGranularity(RollingPeriodGranularity granularity)
    {
        this.granularity = granularity;
    }
    
    public SLARollingPeriod forSLA(SLA sla)
    {
        super.forSLA(sla);
        return this;
    }
    
    public SLARollingPeriod configure(SLARollingPeriodCfg config)
    {
        super.configure(config);
        this.granularity = RollingPeriodGranularity.valueOf(Util.coalesce(config.getGranularity(), RollingPeriodGranularityCfg.monthly).toString().toUpperCase());
        return this;
    }

    @Override
    public SLARollingPeriodMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        SLARollingPeriodMO mo = new SLARollingPeriodMO();
        super.toMO(contact, options, mo);
        mo.setGranularity(this.granularity == null ? null : this.granularity.toString().toUpperCase());
        return mo;
    }
}

package com.intrbiz.bergamot.model;

import java.sql.Date;
import java.util.EnumSet;

import com.intrbiz.bergamot.config.model.SLAFixedPeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.SLAFixedPeriodMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "sla_fixed_period", since = @SQLVersion({4, 0, 0}))
public class SLAFixedPeriod extends SLAPeriod<SLAFixedPeriodMO>
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 5, name = "start", since = @SQLVersion({4, 0, 0}))
    private Date start;

    @SQLColumn(index = 6, name = "end", since = @SQLVersion({4, 0, 0}))
    private Date end;

    public SLAFixedPeriod()
    {
        super();
    }

    public Date getStart()
    {
        return start;
    }

    public void setStart(Date start)
    {
        this.start = start;
    }

    public Date getEnd()
    {
        return end;
    }

    public void setEnd(Date end)
    {
        this.end = end;
    }

    public SLAFixedPeriod forSLA(SLA sla)
    {
        super.forSLA(sla);
        return this;
    }

    public SLAFixedPeriod configure(SLAFixedPeriodCfg config)
    {
        super.configure(config);
        this.start = new Date(config.getStart().getTime());
        this.end = config.getEnd() == null ? null : new Date(config.getEnd().getTime());
        return this;
    }

    @Override
    public SLAFixedPeriodMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        SLAFixedPeriodMO mo = new SLAFixedPeriodMO();
        super.toMO(contact, options, mo);
        mo.setStart(this.start);
        mo.setEnd(this.end);
        return mo;
    }
}

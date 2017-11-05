package com.intrbiz.bergamot.model;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.intrbiz.bergamot.config.model.SLACfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.SLAMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "sla", since = @SQLVersion({ 3, 52, 0 }))
public class SLA extends BergamotObject<SLAMO>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 3, 52, 0 }))
    @SQLPrimaryKey
    private UUID id;
    
    @SQLColumn(index = 2, name = "check_id", since = @SQLVersion({ 3, 52, 0 }))
    @SQLUnique(name = "check_sla_name_unq", columns = { "check_id", "name" })
    private UUID checkId;

    @SQLColumn(index = 3, name = "name", since = @SQLVersion({ 3, 52, 0 }))
    private String name;
    
    @SQLColumn(index = 4, name = "summary", since = @SQLVersion({ 3, 52, 0 }))
    private String summary;
    
    @SQLColumn(index = 5, name = "description", since = @SQLVersion({ 3, 52, 0 }))
    private String description;
    
    @SQLColumn(index = 6, name = "target", since = @SQLVersion({ 3, 52, 0 }))
    private float target;

    public SLA()
    {
        super();
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public float getTarget()
    {
        return target;
    }

    public void setTarget(float target)
    {
        this.target = target;
    }
    
    // helpers
    
    public List<SLAPeriod<?>> getSLAPeriods()
    {
        List<SLAPeriod<?>> periods = new LinkedList<SLAPeriod<?>>();
        try (BergamotDB db = BergamotDB.connect())
        {
            periods.addAll(db.getSLARollingPeriodsForSLA(this.id));
        }
        return periods;
    }
    
    public SLA forCheck(Check<?,?> check)
    {
        this.checkId = check.getId();
        this.id = Site.randomId(Site.getSiteId(this.checkId));
        return this;
    }
    
    public SLA configure(SLACfg config)
    {
        this.name = config.getName();
        this.summary = config.getSummary();
        this.description = config.getDescription();
        this.target = config.getTargetValue();
        return this;
    }

    @Override
    public SLAMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        SLAMO mo = new SLAMO();
        mo.setId(this.id);
        mo.setName(this.name);
        mo.setSummary(this.summary);
        mo.setTarget(this.target);
        if (options.contains(MOFlag.DESCRIPTION)) mo.setDescription(this.description);
        // periods
        for (SLAPeriod<?> period : this.getSLAPeriods())
        {
            mo.getPeriods().add(period.toMO(contact, options));
        }
        return mo;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SLA other = (SLA) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }
}

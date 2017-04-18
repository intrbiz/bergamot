package com.intrbiz.bergamot.model;

import java.util.EnumSet;
import java.util.UUID;

import com.intrbiz.bergamot.model.message.SLAPeriodMO;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

public abstract class SLAPeriod<T extends SLAPeriodMO> extends BergamotObject<T>
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "sla_id", since = @SQLVersion({ 3, 47, 0 }))
    @SQLForeignKey(references = SLA.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID slaId;

    @SQLColumn(index = 2, name = "name", since = @SQLVersion({ 3, 47, 0 }))
    @SQLPrimaryKey
    private String name;

    @SQLColumn(index = 3, name = "summary", since = @SQLVersion({ 3, 47, 0 }))
    private String summary;

    @SQLColumn(index = 4, name = "description", since = @SQLVersion({ 3, 47, 0 }))
    private String description;

    public SLAPeriod()
    {
        super();
    }

    public UUID getSlaId()
    {
        return slaId;
    }

    public void setSlaId(UUID slaId)
    {
        this.slaId = slaId;
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

    protected void toMO(Contact contact, EnumSet<MOFlag> options, SLAPeriodMO mo)
    {
        mo.setName(this.name);
        mo.setSummary(this.summary);
        if (options.contains(MOFlag.DESCRIPTION)) mo.setDescription(this.description);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((slaId == null) ? 0 : slaId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SLAPeriod<?> other = (SLAPeriod<?>) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        if (slaId == null)
        {
            if (other.slaId != null) return false;
        }
        else if (!slaId.equals(other.slaId)) return false;
        return true;
    }
}

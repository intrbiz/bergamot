package com.intrbiz.bergamot.model;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.NamedObjectMO;

/**
 * A generic object with an id and a name
 */
public abstract class NamedObject<T extends NamedObjectMO> extends BergamotObject<T>
{
    protected UUID id = UUID.randomUUID();

    protected String name;

    protected String summary;

    protected String description;

    public NamedObject()
    {
        super();
    }

    public final UUID getId()
    {
        return id;
    }

    public final void setId(UUID id)
    {
        this.id = id;
        this.onSetId();
    }

    protected void onSetId()
    {
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    public final String getSummary()
    {
        return summary;
    }

    public final void setSummary(String summary)
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
        NamedObject<?> other = (NamedObject<?>) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }

    protected void toMO(NamedObjectMO mo, boolean stub)
    {
        mo.setId(this.getId());
        mo.setName(this.getName());
        mo.setSummary(this.getSummary());
        if (! stub) mo.setDescription(this.getDescription());
    }
}

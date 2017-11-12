package com.intrbiz.bergamot.virtual.reference;

import java.util.UUID;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class CheckById<T extends Check<?,?>> implements CheckReference<T>
{
    private static final long serialVersionUID = 1L;
    
    private UUID id;

    public CheckById()
    {
        super();
    }

    public CheckById(UUID id)
    {
        this.id = id;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    public T resolve(VirtualCheckExpressionContext context)
    {
        return (T) context.lookupCheck(this.getId());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CheckById other = (CheckById) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }
}

package com.intrbiz.bergamot.virtual.reference;

import java.util.UUID;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class CheckById implements CheckReference
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

    public Check<?, ?> resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupCheck(this.getId());
    }
}

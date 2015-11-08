package com.intrbiz.bergamot.virtual.reference;

import java.util.UUID;

import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class TrapById extends CheckById implements TrapReference
{
    private static final long serialVersionUID = 1L;
    
    public TrapById()
    {
        super();
    }

    public TrapById(UUID id)
    {
        super(id);
    }
    
    @Override
    public Trap resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupTrap(this.getId());
    }
    
    public String toString()
    {
        return "trap " + this.getId();
    }
}

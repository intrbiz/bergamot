package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface TrapReference extends CheckReference
{
    Trap resolve(VirtualCheckExpressionContext context);
}

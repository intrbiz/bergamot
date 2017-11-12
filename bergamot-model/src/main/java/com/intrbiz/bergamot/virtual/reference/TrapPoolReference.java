package com.intrbiz.bergamot.virtual.reference;

import java.util.List;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface TrapPoolReference extends PoolReference<TrapReference>
{
    List<TrapReference> resolvePool(VirtualCheckExpressionContext context);
}

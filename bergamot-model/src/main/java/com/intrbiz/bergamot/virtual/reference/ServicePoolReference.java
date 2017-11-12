package com.intrbiz.bergamot.virtual.reference;

import java.util.List;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface ServicePoolReference extends PoolReference<ServiceReference>
{
    List<ServiceReference> resolvePool(VirtualCheckExpressionContext context);
}

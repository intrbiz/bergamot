package com.intrbiz.bergamot.virtual.reference;

import java.util.List;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface HostPoolReference extends PoolReference<HostReference>
{
    List<HostReference> resolvePool(VirtualCheckExpressionContext context);
}

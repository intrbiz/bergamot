package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface ResourceReference extends CheckReference
{
    Resource resolve(VirtualCheckExpressionContext context);
}

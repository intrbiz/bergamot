package com.intrbiz.bergamot.virtual.reference;


import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface ServiceReference extends CheckReference
{
    Service resolve(VirtualCheckExpressionContext context);
}

package com.intrbiz.bergamot.virtual.reference;


import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

public interface ServiceReference extends CheckReference
{
    Service resolve(VirtualCheckExpressionParserContext context);
}

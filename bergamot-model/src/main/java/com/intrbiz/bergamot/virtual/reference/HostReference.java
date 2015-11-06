package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

public interface HostReference extends CheckReference
{
    Host resolve(VirtualCheckExpressionParserContext context);
}

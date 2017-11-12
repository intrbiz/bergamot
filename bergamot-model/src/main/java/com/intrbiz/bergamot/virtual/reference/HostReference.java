package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface HostReference extends CheckReference<Host>
{
    Host resolve(VirtualCheckExpressionContext context);
}

package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

public interface TrapReference extends CheckReference
{
    Trap resolve(VirtualCheckExpressionParserContext context);
}

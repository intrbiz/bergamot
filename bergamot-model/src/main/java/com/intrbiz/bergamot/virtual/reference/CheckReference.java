package com.intrbiz.bergamot.virtual.reference;

import java.io.Serializable;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

public interface CheckReference extends Serializable
{
    /**
     * Resolve the check referenced by this reference
     * @return the check
     */
    Check<?,?> resolve(VirtualCheckExpressionParserContext context);
}

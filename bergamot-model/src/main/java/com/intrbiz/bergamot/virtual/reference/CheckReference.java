package com.intrbiz.bergamot.virtual.reference;

import java.io.Serializable;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface CheckReference<T extends Check<?,?>> extends Serializable
{
    /**
     * Resolve the check referenced by this reference
     * @return the check
     */
    T resolve(VirtualCheckExpressionContext context);
}

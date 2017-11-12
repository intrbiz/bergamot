package com.intrbiz.bergamot.virtual.reference;

import java.io.Serializable;
import java.util.List;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface PoolReference<T extends CheckReference<?>> extends Serializable
{
    String getPool();
    
    /**
     * Resolve the checks referenced by this reference
     * @return the checks
     */
    List<T> resolvePool(VirtualCheckExpressionContext context);
}

package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public interface ClusterReference extends CheckReference
{
    Cluster resolve(VirtualCheckExpressionContext context);
}

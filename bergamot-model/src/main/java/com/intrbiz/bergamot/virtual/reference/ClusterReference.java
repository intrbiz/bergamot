package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

public interface ClusterReference extends CheckReference
{
    Cluster resolve(VirtualCheckExpressionParserContext context);
}

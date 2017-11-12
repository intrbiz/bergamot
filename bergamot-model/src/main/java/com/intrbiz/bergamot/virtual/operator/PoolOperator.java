package com.intrbiz.bergamot.virtual.operator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;
import com.intrbiz.bergamot.virtual.reference.PoolReference;

public class PoolOperator extends ValuesOperator
{
    private static final long serialVersionUID = 1L;
    
    private final PoolReference<?> pool;
    
    public PoolOperator(PoolReference<?> pool)
    {
        super();
        this.pool = pool;
    }
    
    public PoolReference<?> getPool()
    {
        return this.pool;
    }
    
    public void computeDependencies(VirtualCheckExpressionContext context,  Set<CheckReference<?>> checks)
    {
        checks.addAll(this.pool.resolvePool(context));
    }
    
    @Override
    public List<ValueOperator> getValues(VirtualCheckExpressionContext context)
    {
        return this.pool.resolvePool(context).stream()
                .map((r) -> new ValueOperator(r)).collect(Collectors.toList());
    }

    @Override
    public void computePoolDependencies(VirtualCheckExpressionContext context, Set<String> pools)
    {
        pools.add(this.pool.getPool());
    }

    public String toString()
    {
       return this.pool.toString();
    }
}

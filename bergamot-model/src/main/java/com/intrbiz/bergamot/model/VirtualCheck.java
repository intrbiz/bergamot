package com.intrbiz.bergamot.model;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.intrbiz.bergamot.config.model.VirtualCheckCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.VirtualCheckOperatorAdapter;
import com.intrbiz.bergamot.model.message.VirtualCheckMO;
import com.intrbiz.bergamot.virtual.operator.VirtualCheckOperator;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A virtual check is conditional upon the state of 
 * other checks
 */
public abstract class VirtualCheck<T extends VirtualCheckMO, C extends VirtualCheckCfg<C>> extends Check<T,C>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Checks which this check references
     */
    @SQLColumn(index = 1, name = "reference_ids", type = "UUID[]", since = @SQLVersion({4, 0, 0}))
    protected List<UUID> referenceIds = new LinkedList<UUID>();
    
    /**
     * The virtual check condition used to compute the state of this check
     */
    @SQLColumn(index = 2, name = "condition", type = "TEXT", adapter = VirtualCheckOperatorAdapter.class, since = @SQLVersion({4, 0, 0}))
    private VirtualCheckOperator condition;
    
    /**
     * Pools which this check references
     */
    @SQLColumn(index = 3, name = "reference_resource_pools", type = "text[]", since = @SQLVersion({4, 0, 0}))
    protected List<String> referenceResourcePools = new LinkedList<String>();
    
    public VirtualCheck()
    {
        super();
    }
    
    public List<UUID> getReferenceIds()
    {
        return referenceIds;
    }

    public void setReferenceIds(List<UUID> referenceIds)
    {
        this.referenceIds = referenceIds;
    }
    
    public List<String> getReferenceResourcePools()
    {
        return referenceResourcePools;
    }

    public void setReferenceResourcePools(List<String> referenceResourcePools)
    {
        this.referenceResourcePools = referenceResourcePools;
    }
    
    public List<Check<?,?>> getReferences()
    {
        List<Check<?,?>> r = new LinkedList<Check<?,?>>();
        if (this.getReferenceIds() != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                for (UUID refId : this.getReferenceIds())
                {
                    r.add(db.getCheck(refId));
                }
            }
        }
        return r;
    }
    
    public void addReference(Check<?,?> check)
    {
        this.referenceIds.add(check.getId());
    }

    public VirtualCheckOperator getCondition()
    {
        return condition;
    }

    public void setCondition(VirtualCheckOperator condition)
    {
        this.condition = condition;
    }
}

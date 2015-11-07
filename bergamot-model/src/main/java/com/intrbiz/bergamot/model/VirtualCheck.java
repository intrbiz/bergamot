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
    @SQLColumn(index = 1, name = "reference_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    protected List<UUID> referenceIds = new LinkedList<UUID>();
    
    @SQLColumn(index = 2, name = "condition", type = "TEXT", adapter = VirtualCheckOperatorAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    private VirtualCheckOperator condition;
    
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
    
    public List<Check<?,?>> getReferences()
    {
        List<Check<?,?>> r = new LinkedList<Check<?,?>>();
        if (this.getReferenceIds() != null)
        {
            for (UUID refId : this.getReferenceIds())
            {
                try (BergamotDB db = BergamotDB.connect())
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

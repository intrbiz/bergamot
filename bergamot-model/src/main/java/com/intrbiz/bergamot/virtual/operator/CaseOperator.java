package com.intrbiz.bergamot.virtual.operator;

import java.util.List;
import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class CaseOperator extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;

    private final List<CaseWhen> when;
    
    private final Status elseStatus;

    public CaseOperator(List<CaseWhen> when, Status elseStatus)
    {
        super();
        this.when = when;
        this.elseStatus = elseStatus == null ? Status.CRITICAL : elseStatus;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        return this.computeStatus(context).isOk();
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        for (CaseWhen test : this.when)
        {
            Status status = test.apply(context);
            if (status != null) return status;
        }
        return this.elseStatus;
    }

    @Override
    public void computeDependencies(Set<CheckReference> checks)
    {
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder("case");
        for (CaseWhen cw : this.when)
        {
            sb.append(" ").append(cw.toString());
        }
        sb.append(" else ").append(this.elseStatus.toString()).append(" end");
        return sb.toString();
    }
    
    public static class CaseWhen
    {
        private final VirtualCheckOperator value;
        
        private final Status status;
        
        private final Status as;
        
        public CaseWhen(VirtualCheckOperator value, Status status, Status as)
        {
            this.value = value;
            this.status = status;
            this.as = as;
        }

        public VirtualCheckOperator getValue()
        {
            return value;
        }

        public Status getStatus()
        {
            return status;
        }

        public Status getAs()
        {
            return as;
        }
        
        public Status apply(VirtualCheckExpressionContext context)
        {
            return this.value.computeStatus(context) == this.status ? this.as : null;
        }
        
        public String toString()
        {
            return "when " + this.value.toString() + " is " + this.status.toString() + " then " + this.as;
        }
    }
}

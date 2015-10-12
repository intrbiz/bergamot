package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ProcessResultAccountingEvent extends BergamotAccountingEvent
{
    public static final UUID TYPE_ID = UUID.fromString("30108f43-34d6-4338-b652-48f1a0c08565");
    
    public static enum ResultType { ACTIVE, PASSIVE }
    
    private UUID executionId;
    
    private UUID checkId;
    
    private ResultType resultType;
    
    public ProcessResultAccountingEvent()
    {
        super();
    }
    
    public ProcessResultAccountingEvent(long timestamp, UUID siteId, UUID executionId, UUID checkId, ResultType resultType)
    {
        super(timestamp, siteId);
        this.executionId = executionId;
        this.checkId = checkId;
        this.resultType = resultType;
    }
    
    public ProcessResultAccountingEvent(UUID siteId, UUID executionId, UUID checkId, ResultType resultType)
    {
        super(siteId);
        this.executionId = executionId;
        this.checkId = checkId;
        this.resultType = resultType;
    }

    @Override
    public final UUID getTypeId()
    {
        return TYPE_ID;
    }

    public UUID getExecutionId()
    {
        return executionId;
    }

    public void setExecutionId(UUID executionId)
    {
        this.executionId = executionId;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public ResultType getResultType()
    {
        return resultType;
    }

    public void setResultType(ResultType resultType)
    {
        this.resultType = resultType;
    }

    public String toString()
    {
        return super.toString() + " [" + this.executionId + "] [" + this.checkId + "] [" + this.resultType + "]";
    }

    @Override
    public void pack(ByteBuffer into)
    {
        super.pack(into);
        this.packUUID(this.executionId, into);
        this.packUUID(this.checkId, into);
        into.putInt(this.resultType == null ? -1 : this.resultType.ordinal());
    }

    @Override
    public void unpack(ByteBuffer from)
    {
        super.unpack(from);
        this.executionId = this.unpackUUID(from);
        this.checkId = this.unpackUUID(from);
        int rType = from.getInt();
        this.resultType = rType == -1 ? null : ResultType.values()[rType]; 
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((checkId == null) ? 0 : checkId.hashCode());
        result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ProcessResultAccountingEvent other = (ProcessResultAccountingEvent) obj;
        if (checkId == null)
        {
            if (other.checkId != null) return false;
        }
        else if (!checkId.equals(other.checkId)) return false;
        if (executionId == null)
        {
            if (other.executionId != null) return false;
        }
        else if (!executionId.equals(other.executionId)) return false;
        if (resultType != other.resultType) return false;
        return true;
    }
}

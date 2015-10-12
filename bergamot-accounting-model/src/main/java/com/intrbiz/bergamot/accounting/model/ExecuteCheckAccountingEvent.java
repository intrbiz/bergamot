package com.intrbiz.bergamot.accounting.model;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ExecuteCheckAccountingEvent extends BergamotAccountingEvent
{
    public static final UUID TYPE_ID = UUID.fromString("ab52d8b1-b43f-4dd7-9b9b-9bde92da1fde");
    
    private UUID executionId;
    
    private UUID checkId;
    
    private String engine;
    
    private String executor;
    
    private String command;
    
    public ExecuteCheckAccountingEvent()
    {
        super();
    }
    
    public ExecuteCheckAccountingEvent(long timestamp, UUID siteId, UUID executionId, UUID checkId, String engine, String executor, String command)
    {
        super(timestamp, siteId);
        this.executionId = executionId;
        this.checkId = checkId;
        this.engine = engine;
        this.executor = executor;
        this.command = command;
    }
    
    public ExecuteCheckAccountingEvent(UUID siteId, UUID executionId, UUID checkId, String engine, String executor, String command)
    {
        super(siteId);
        this.executionId = executionId;
        this.checkId = checkId;
        this.engine = engine;
        this.executor = executor;
        this.command = command;
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

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }
    
    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public String toString()
    {
        return super.toString() + " [" + this.executionId + "] [" + this.checkId + "] [" + this.engine + "] [" + this.executor + "] [" + this.command + "]";
    }

    @Override
    public void pack(ByteBuffer into)
    {
        super.pack(into);
        this.packUUID(this.executionId, into);
        this.packUUID(this.checkId, into);
        this.packString(this.engine, into);
        this.packString(this.executor, into);
        this.packString(this.command, into);
    }

    @Override
    public void unpack(ByteBuffer from)
    {
        super.unpack(from);
        this.executionId = this.unpackUUID(from);
        this.checkId = this.unpackUUID(from);
        this.engine = this.unpackString(from);
        this.executor = this.unpackString(from);
        this.command = this.unpackString(from);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((checkId == null) ? 0 : checkId.hashCode());
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((engine == null) ? 0 : engine.hashCode());
        result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
        result = prime * result + ((executor == null) ? 0 : executor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ExecuteCheckAccountingEvent other = (ExecuteCheckAccountingEvent) obj;
        if (checkId == null)
        {
            if (other.checkId != null) return false;
        }
        else if (!checkId.equals(other.checkId)) return false;
        if (command == null)
        {
            if (other.command != null) return false;
        }
        else if (!command.equals(other.command)) return false;
        if (engine == null)
        {
            if (other.engine != null) return false;
        }
        else if (!engine.equals(other.engine)) return false;
        if (executionId == null)
        {
            if (other.executionId != null) return false;
        }
        else if (!executionId.equals(other.executionId)) return false;
        if (executor == null)
        {
            if (other.executor != null) return false;
        }
        else if (!executor.equals(other.executor)) return false;
        return true;
    }
}

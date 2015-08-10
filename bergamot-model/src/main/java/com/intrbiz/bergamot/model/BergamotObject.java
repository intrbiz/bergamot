package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.util.EnumSet;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.MessageObject;

public abstract class BergamotObject<T extends MessageObject> implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;
    
    public enum MOFlag
    {
        DESCRIPTION,
        PARAMETERS,
        COMMENTS,
        GROUPS,
        CONTACTS,
        TEAMS,
        NOTIFICATIONS,
        REFERENCED_BY,
        REFERENCES,
        DOWNTIME,
        COMMAND,
        STATS,
        EXCLUDES,
        RANGES,
        CHILDREN,
        CHECKS,
        LOCATION,
        HOSTS,
        SERVICES,
        TRAPS,
        RESOURCES,
        HOST,
        CLUSTER;
        
        public static final EnumSet<MOFlag> ALL = EnumSet.allOf(MOFlag.class);
        
        public static final EnumSet<MOFlag> STUB = EnumSet.noneOf(MOFlag.class);
    }
    
    public BergamotObject()
    {
        super();
    }
    
    public final T toMOUnsafe()
    {
        return this.toMO(null);
    }

    public final T toMO(Contact contact)
    {
        return this.toMO(contact, MOFlag.ALL);
    }
    
    public final T toStubMOUnsafe()
    {
        return this.toStubMO(null);
    }

    public final T toStubMO(Contact contact)
    {
        return this.toMO(contact, MOFlag.STUB);
    }

    public abstract T toMO(Contact contact, EnumSet<MOFlag> options);

    public String toJSON()
    {
        return new BergamotTranscoder().encodeAsString(this.toMO(null));
    }
}

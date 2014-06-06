package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.MessageObject;

public abstract class BergamotObject<T extends MessageObject>
{
    public BergamotObject()
    {
        super();
    }

    public final T toMO()
    {
        return this.toMO(false);
    }

    public final T toStubMO()
    {
        return this.toMO(true);
    }

    public T toMO(boolean stub)
    {
        return null;
    }

    public String toJSON()
    {
        return new BergamotTranscoder().encodeAsString(this.toMO());
    }
}

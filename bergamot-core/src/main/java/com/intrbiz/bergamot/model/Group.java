package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.model.state.GroupState;

public abstract class Group extends NamedObject
{
    public Group()
    {
        super();
    }
    
    public abstract GroupState getState();
}

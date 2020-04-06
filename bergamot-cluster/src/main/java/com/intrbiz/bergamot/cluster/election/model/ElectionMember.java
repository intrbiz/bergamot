package com.intrbiz.bergamot.cluster.election.model;

public class ElectionMember
{
    private final ElectionState state;
    
    private final int position;

    public ElectionMember(ElectionState state, int position)
    {
        super();
        this.state = state;
        this.position = position;
    }

    public ElectionState getState()
    {
        return this.state;
    }

    public int getPosition()
    {
        return this.position;
    }
}

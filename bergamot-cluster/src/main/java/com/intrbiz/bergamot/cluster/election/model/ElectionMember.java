package com.intrbiz.bergamot.cluster.election.model;

import java.util.UUID;

public class ElectionMember
{   
    private final UUID id;
    
    private final ElectionState state;
    
    private final int position;
    
    private final String path;

    public ElectionMember(UUID id, ElectionState state, int position, String path)
    {
        super();
        this.id = id;
        this.state = state;
        this.position = position;
        this.path = path;
    }

    public UUID getId()
    {
        return this.id;
    }

    public ElectionState getState()
    {
        return this.state;
    }

    public int getPosition()
    {
        return this.position;
    }

    public String getPath()
    {
        return this.path;
    }
    
    public String toString()
    {
        return this.path + "(" + this.id + " " + this.state + " " + this.position + ")";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ElectionMember other = (ElectionMember) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }
}

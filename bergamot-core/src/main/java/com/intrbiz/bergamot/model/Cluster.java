package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * A cluster of resources spanning many hosts
 */
public class Cluster extends VirtualCheck
{
    public Cluster()
    {
        super();
    }

    @Override
    public String getType()
    {
        return "cluster";
    }

    @Override
    public CheckMO toMO()
    {
        return null;
    }
}

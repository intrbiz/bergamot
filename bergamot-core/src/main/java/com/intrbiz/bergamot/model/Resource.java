package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * A resource of a cluster, which is provided by multiple services
 */
public class Resource extends VirtualCheck
{
    public Resource()
    {
        super();
    }

    @Override
    public String getType()
    {
        return "resource";
    }

    @Override
    public CheckMO toMO()
    {
        return null;
    }
    
    
}

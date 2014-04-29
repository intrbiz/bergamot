package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.model.message.TrapMO;

/**
 * The real world manifestation of a passive check
 */
public class Trap extends PassiveCheck
{
    private Host host;

    public Trap()
    {
        super();
    }

    @Override
    public final String getType()
    {
        return "trap";
    }

    public Host getHost()
    {
        return host;
    }

    public void setHost(Host host)
    {
        this.host = host;
    }

    @Override
    public TrapMO toMO()
    {
        TrapMO mo = new TrapMO();
        super.toMO(mo);
        mo.setHost(this.getHost().toMO());
        return mo;
    }
}

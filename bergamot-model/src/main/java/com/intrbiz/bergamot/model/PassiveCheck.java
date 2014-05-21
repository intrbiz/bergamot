package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.model.message.PassiveCheckMO;

/**
 * A check which is never polled
 */
public abstract class PassiveCheck<T extends PassiveCheckMO> extends RealCheck<T>
{
    public PassiveCheck()
    {
        super();
    }
}

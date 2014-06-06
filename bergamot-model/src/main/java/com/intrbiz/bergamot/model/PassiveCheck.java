package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.config.model.PassiveCheckCfg;
import com.intrbiz.bergamot.model.message.PassiveCheckMO;

/**
 * A check which is never polled
 */
public abstract class PassiveCheck<T extends PassiveCheckMO, C extends PassiveCheckCfg<C>> extends RealCheck<T,C>
{
    public PassiveCheck()
    {
        super();
    }
}

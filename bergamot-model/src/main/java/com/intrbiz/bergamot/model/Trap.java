package com.intrbiz.bergamot.model;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.configuration.Configurable;

/**
 * The real world manifestation of a passive check
 */
public class Trap extends PassiveCheck implements Configurable<TrapCfg>
{
    private Host host;
    
    private TrapCfg config;

    public Trap()
    {
        super();
    }
    
    @Override
    public void configure(TrapCfg cfg)
    {
        this.config = cfg;
        TrapCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.displayName = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.alertAttemptThreshold = rcfg.getState().getFailedAfter();
        this.recoveryAttemptThreshold = rcfg.getState().getRecoversAfter();
        this.enabled = rcfg.getEnabledBooleanValue();
        this.suppressed = rcfg.getSuppressedBooleanValue();
        // initial state
        this.getState().setAttempt(this.recoveryAttemptThreshold);
        if (rcfg.getInitialState() != null)
        {
            this.getState().setStatus(Status.valueOf(rcfg.getInitialState().getStatus().toUpperCase()));
            this.getState().setOk(this.getState().getStatus().isOk());
            this.getState().setOutput(Util.coalesce(rcfg.getInitialState().getOutput(), ""));
            this.getState().setLastHardStatus(this.getState().getStatus());
            this.getState().setLastHardOk(this.getState().isOk());
            this.getState().setLastHardOutput(this.getState().getOutput());
        }
    }

    @Override
    public TrapCfg getConfiguration()
    {
        return this.config;
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

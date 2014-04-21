package com.intrbiz.bergamot.scheduler;

import com.intrbiz.bergamot.config.SchedulerCfg;
import com.intrbiz.bergamot.engine.AbstractEngine;

public abstract class AbstractScheduler extends AbstractEngine<SchedulerCfg> implements Scheduler
{    
    public AbstractScheduler()
    {
        super();
    }
}

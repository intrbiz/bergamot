package com.intrbiz.bergamot.scheduler;

import com.intrbiz.bergamot.component.AbstractComponent;
import com.intrbiz.bergamot.config.SchedulerCfg;

public abstract class AbstractScheduler extends AbstractComponent<SchedulerCfg> implements Scheduler
{    
    public AbstractScheduler()
    {
        super();
    }
}

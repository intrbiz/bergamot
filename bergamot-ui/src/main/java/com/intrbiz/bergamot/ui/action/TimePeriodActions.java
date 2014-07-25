package com.intrbiz.bergamot.ui.action;

import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.metadata.Action;

public class TimePeriodActions
{
    @Action("create-timeperiod")
    public TimePeriod createTimePeriod(TimePeriodCfg config)
    {
        // TODO: store the config
        // create the time period
        TimePeriod timePeriod = new TimePeriod();
        timePeriod.configure(config);
        // TODO: store the time period
        return timePeriod;
    }
}

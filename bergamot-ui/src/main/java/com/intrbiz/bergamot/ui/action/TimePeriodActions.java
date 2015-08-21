package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.metadata.Action;

public class TimePeriodActions
{
    private Logger logger = Logger.getLogger(TimePeriodActions.class);
    
    @Action("create-time-period")
    public TimePeriod createTimePeriod(TimePeriodCfg config)
    {
        if (config.getId() == null) throw new IllegalArgumentException("Config must have a valid ID");
        try (BergamotDB db = BergamotDB.connect())
        {
            // resolve the config
            db.getConfigResolver(Site.getSiteId(config.getId())).computeInheritenance(config);
            // store the config
            db.setConfig(new Config(config.getId(), Site.getSiteId(config.getId()), config));
            // create the time period
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.configure(config);
            // store it?
            if (! config.getTemplateBooleanValue())
            {
                logger.info("Storing TimePeriod: " + timePeriod.toJSON());
                db.setTimePeriod(timePeriod);
            }
            return timePeriod;
        }
    }
}

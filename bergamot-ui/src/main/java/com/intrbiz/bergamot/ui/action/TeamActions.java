package com.intrbiz.bergamot.ui.action;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.metadata.Action;

public class TeamActions
{
    private Logger logger = Logger.getLogger(TeamActions.class);
    
    @Action("create-team")
    public Team createTeam(TeamCfg config)
    {
        if (config.getId() == null) throw new IllegalArgumentException("Config must have a valid Id");
        try (BergamotDB db = BergamotDB.connect())
        {
            // resolve the config
            db.getConfigResolver(Site.getSiteId(config.getId())).computeInheritenance(config);
            // store the config
            db.setConfig(new Config(config.getId(), Site.getSiteId(config.getId()), config));
            // create the team
            Team team = new Team();
            team.configure(config);
            // store it?
            if (! config.getTemplateBooleanValue())
            {
                logger.info("Storing Team: " + team.toJSON());
                db.setTeam(team);
            }
            return team;
        }
    }
}

package com.intrbiz.bergamot.ui.action;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotConfigImporter;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.metadata.Action;


public class ConfigChangeActions
{
    private Logger logger = Logger.getLogger(ConfigChangeActions.class);

    @Action("apply-config-change")
    public BergamotImportReport applyConfigChange(UUID siteId, UUID changeId)
    {
        logger.info("Applying configuration change: " + siteId + "::" + changeId);
        try (BergamotDB db = BergamotDB.connect())
        {
            ConfigChange change = db.getConfigChange(changeId);
            // validate
            BergamotCfg cfg = (BergamotCfg) change.getConfiguration();
            ValidatedBergamotConfiguration validated = cfg.validate(db.getObjectLocator(siteId));
            // import
            BergamotImportReport report = new BergamotConfigImporter(validated).resetState(false).online(true).importConfiguration();
            // update the db
            if (report.isSuccessful())
            {
                change.setApplied(true);
                change.setAppliedAt(new Timestamp(System.currentTimeMillis()));
                db.setConfigChange(change);
            }
            // return the report
            logger.info("Configuration change applied:\n" + report.toString());
            return report;
        }
    }
    
}

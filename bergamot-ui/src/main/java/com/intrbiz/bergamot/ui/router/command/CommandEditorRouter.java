package com.intrbiz.bergamot.ui.router.command;

import java.io.StringReader;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.api.APIResponse;
import com.intrbiz.bergamot.model.message.api.APIResponse.Stat;
import com.intrbiz.bergamot.model.message.api.call.VerifiedCommand;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.config.BergamotValidationReportMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/command/editor")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class CommandEditorRouter extends Router<BergamotApp>
{    
    private Logger logger = Logger.getLogger(CommandEditorRouter.class);
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        encode("command/editor");
    }
    
    @Any("/verify")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public APIResponse verifyCommand(BergamotDB db, @SessionVar("site") Site site, @Param("command") String commandDef)
    {
        try
        {
            // parse the command
            CommandCfg command = Configuration.read(CommandCfg.class, new StringReader(commandDef));
            // wrap the config
            BergamotCfg config = new BergamotCfg();
            config.addObject(command);
            // validate the config
            ValidatedBergamotConfiguration validated = config.validate(db.getObjectLocator(site.getId()));
            if (validated.getReport().isValid())
            {
                // we need the fully resolved command
                CommandCfg resolved = command.resolve();
                // render the parameters view
                var("command", resolved);
                String view = this.encodeOnlyBuffered("command/parameters");
                // build the check skeleton
                ExecuteCheck skeleton = new ExecuteCheck();
                skeleton.setEngine(resolved.getEngine());
                skeleton.setExecutor(resolved.getExecutor());
                skeleton.setName(resolved.getName());
                skeleton.setScript(resolved.getScript());
                skeleton.setTimeout(60_000L);
                // all verified
                return new VerifiedCommand(Stat.OK, new BergamotValidationReportMO(validated.getReport().getSite(), validated.getReport().isValid(), validated.getReport().getWarnings(), validated.getReport().getErrors()), view, skeleton);
            }
            else
            {
                return new VerifiedCommand(Stat.ERROR, new BergamotValidationReportMO(validated.getReport().getSite(), validated.getReport().isValid(), validated.getReport().getWarnings(), validated.getReport().getErrors()), null, null);
            }
        }
        catch (Exception e)
        {
            logger.error("Error verifing command", e);
            return new APIError(e.getMessage());
        }
    }
}

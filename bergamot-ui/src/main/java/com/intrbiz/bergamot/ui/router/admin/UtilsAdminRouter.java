package com.intrbiz.bergamot.ui.router.admin;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;

@Prefix("/admin/utils")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
@RequirePermission("ui.admin.cluster")
public class UtilsAdminRouter extends Router<BergamotApp>
{   
    private Logger logger = Logger.getLogger(UtilsAdminRouter.class);
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        encode("admin/utils/index");
    }
    
    @Any("/cache/clear")
    @WithDataAdapter(BergamotDB.class)
    public void clearCaches(BergamotDB db, @SessionVar("site") Site site)
    {
        logger.warn("Flushing all data caches due to administrative request");
        db.cacheClear();
        var("cacheCleared", true);
        encode("admin/utils/index");
    }
}

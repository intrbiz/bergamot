package com.intrbiz.bergamot.ui.router.global;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.ui.BergamotUI;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.Cache;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/global/admin/utils")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class GlobalUtilsAdminRouter extends Router<BergamotUI>
{   
    private static final Logger logger = Logger.getLogger(GlobalUtilsAdminRouter.class);
    
    @Any("/")
    public void index()
    {
        encode("global/admin/utils/index");
    }
    
    @Any("/cache/clear")
    @WithDataAdapter(BergamotDB.class)
    public void clearCaches(BergamotDB db)
    {
        logger.warn("Flushing all data caches due to administrative request");
        db.cacheClear();
        var("cacheCleared", true);
        encode("global/admin/utils/index");
    }
    
    @Any("/cache/view")
    @WithDataAdapter(BergamotDB.class)
    public void viewCache(BergamotDB db)
    {
        Cache cache = DataManager.get().cache("cache.bergamot");
        var("cachekeys", cache.keySet(""));
        encode("global/admin/utils/cache/view");
    }
}

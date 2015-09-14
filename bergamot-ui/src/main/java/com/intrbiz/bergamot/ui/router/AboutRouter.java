package com.intrbiz.bergamot.ui.router;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/about")
@Template("layout/main")
@RequireValidPrincipal()
public class AboutRouter extends Router<BergamotApp>
{    
    @Any("/")
    public void about()
    {
        var("bergamot_version",  BergamotApp.VERSION.NUMBER);
        var("bergamot_codename", BergamotApp.VERSION.CODE_NAME);
        encode("about");
    }
    
    @Any("/routes")
    public void routes()
    {
        List<Route> routes = var("routes", new LinkedList<Route>());
        // introspect our routes
        for (Router<?> router : app().getRouters())
        {
            String prefix = router.getPrefix();
            for (Route route : Route.fromRouter(prefix, router))
            {
                if ((! route.isFilter()) && (! route.isExceptionHandler())) 
                    routes.add(route);
            }
        }
        encode("docs/routes");
    }
}

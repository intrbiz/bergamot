package com.intrbiz.bergamot.ui.router;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Param;
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
        // introspect our routes
        List<RouteInfo> routes = var("routes", new LinkedList<RouteInfo>());
        for (Router<?> router : app().getRouters())
        {
            String prefix = router.getPrefix();
            for (Route route : Route.fromRouter(prefix, router))
            {
                if ((! route.isFilter()) && (! route.isExceptionHandler())) 
                    routes.add(new RouteInfo(route));
            }
        }
        encode("docs/routes");
    }
    
    public static class RouteInfo
    {
        private String method;
        
        private String prefix;
        
        private String pattern;
        
        private LinkedHashSet<String> as;
        
        private List<String> parameters;
        
        public RouteInfo(Route route)
        {
            this.method = route.getMethod();
            this.prefix = route.getPrefix();
            this.pattern = route.getPattern();
            this.as = new LinkedHashSet<String>(Arrays.asList(route.getCompiledPattern().getAs()));
            this.parameters = new LinkedList<String>();
            // look at the annotations
            for (Parameter parameter : route.getHandler().getParameters())
            {
                Param param = parameter.getAnnotation(Param.class);
                if (param != null)
                {
                    if (! this.as.contains(param.value())) 
                        this.parameters.add(param.value());
                    continue;
                }
                ListParam listParam = parameter.getAnnotation(ListParam.class);
                if (listParam != null)
                {
                    if (! this.as.contains(listParam.value()))
                        this.parameters.add(listParam.value());
                    continue;
                }
            }
        }
        
        public String getMethod()
        {
            return this.method;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public String getPattern()
        {
            return pattern;
        }

        public LinkedHashSet<String> getAs()
        {
            return as;
        }

        public List<String> getParameters()
        {
            return parameters;
        }
    }
}

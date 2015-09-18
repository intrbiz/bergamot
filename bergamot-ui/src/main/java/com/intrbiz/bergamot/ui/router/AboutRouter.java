package com.intrbiz.bergamot.ui.router;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.impl.route.Route;
import com.intrbiz.balsa.engine.impl.route.exec.ExecBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ArgumentBuilder;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ListParameterArgument;
import com.intrbiz.balsa.engine.impl.route.exec.argument.ParameterArgument;
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
    public void routes() throws Exception
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
        
        private List<RouteParameter> parameters;
        
        private Class<?> returnType;
        
        private String callName;
        
        public RouteInfo(Route route) throws Exception
        {
            this.method = route.getMethod();
            this.prefix = route.getPrefix();
            this.pattern = route.getPattern();
            this.as = new LinkedHashSet<String>(Arrays.asList(route.getCompiledPattern().getAs()));
            this.parameters = new LinkedList<RouteParameter>();
            this.callName = route.getHandler().getName();
            this.returnType = route.getHandler().getReturnType();
            // use the exec build to introspect the route
            ExecBuilder exec = ExecBuilder.build(route);
            // look at the annotations
            Parameter[] parameters = route.getHandler().getParameters();
            for (int i = 0; i < exec.getArguments().length; i++)
            {
                ArgumentBuilder<?> builder = exec.getArguments()[i];
                Parameter parameter = parameters[i];
                if (builder instanceof ParameterArgument)
                {
                    ParameterArgument paramArg = (ParameterArgument) builder;
                    this.parameters.add(new RouteParameter(paramArg.getParameterName(), parameter.getName(), parameter.getType(), this.as.contains(paramArg.getParameterName())));
                }
                else if (builder instanceof ListParameterArgument)
                {
                    ListParameterArgument paramArg = (ListParameterArgument) builder;
                    this.parameters.add(new RouteParameter(paramArg.getParameterName(), parameter.getName(), parameter.getType(), false));
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

        public List<RouteParameter> getParameters()
        {
            return parameters;
        }
        
        public Class<?> getReturnType()
        {
            return returnType;
        }

        public String getCallName()
        {
            return callName;
        }

        public String buildRouteAPICall()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("package com.intrbiz.bergamot.call;\n");
            sb.append("\n");
            sb.append("import java.io.IOException;\n");
            sb.append("import org.apache.http.client.fluent.*;\n");
            sb.append("import com.intrbiz.bergamot.*;\n");
            sb.append("import com.intrbiz.bergamot.model.message.*;\n");
            for (RouteParameter param : this.parameters)
            {
                sb.append("import ").append(param.getType().getCanonicalName()).append(";\n");    
            }
            sb.append("import ").append(this.returnType.getCanonicalName()).append(";\n");
            sb.append("\n");
            sb.append("public class ").append(Util.ucFirst(this.callName)).append("Call extends BergamotAPICall<").append(this.returnType.getSimpleName()).append(">\n");
            sb.append("{\n");
            // parameters
            for (RouteParameter parameter : this.parameters)
            {
                sb.append("\n");
                sb.append("    private ").append(parameter.getType().getSimpleName()).append(" ").append(parameter.getFieldName()).append("\n");
            }
            sb.append("\n");
            for (RouteParameter parameter : this.parameters)
            {
                sb.append("\n");
                sb.append("    public ").append(Util.ucFirst(this.callName)).append("Call ").append(parameter.getFieldName())
                 .append("(").append(parameter.getType().getSimpleName()).append(" ").append(parameter.getFieldName()).append(")\n");
                sb.append("    {\n");
                sb.append("        this.").append(parameter.getFieldName()).append(" = ").append(parameter.getFieldName()).append("\n");
                sb.append("        return this;\n");
                sb.append("    }\n");
            }
            //
            String url = "\"" + (this.prefix.endsWith("/") ? this.prefix.substring(0, this.prefix.length() -1) : this.prefix) + this.pattern + "\"";
            for (RouteParameter param : this.parameters)
            {
                if (param.isAs()) 
                    url = url.replace(":" + param.getName(), "\" + this." + param.getFieldName() + " + \"");
            }
            //
            sb.append("\n");
            sb.append("    public ").append(this.getReturnType().getSimpleName()).append(" execute()\n");
            sb.append("    {\n");
            sb.append("        try\n");
            sb.append("        {\n");
            sb.append("            Response response = execute(\n");
            if ("post".equalsIgnoreCase(this.method) || "any".equalsIgnoreCase(this.method))
            {
                sb.append("                post(url(").append(url).append("))\n");
                sb.append("                .addHeader(authHeader())\n");
                sb.append("                .bodyForm(\n");
                boolean ns = false;
                for (RouteParameter param : this.parameters)
                {
                    if (! param.isAs())
                    {
                        if (ns) sb.append(",\n");
                        sb.append("                    param(\"").append(param.getName()).append("\", this.").append(param.getFieldName()).append(")");
                        ns = true;
                    }
                }
                sb.append("\n");
                sb.append("                )\n");
            }
            else
            {
                if (this.parameters.stream().anyMatch((p) -> ! p.isAs()))
                {
                    sb.append("                get(url(").append(url).append("))\n");
                    sb.append("                .addHeader(authHeader())\n");
                }
                else
                {
                    sb.append("                get(\n");
                    sb.append("                    appendQuery(\n");
                    sb.append("                        url(").append(url).append(")");
                    for (RouteParameter param : this.parameters)
                    {
                        if (! param.isAs())
                        {
                            sb.append(",\n");
                            sb.append("                    param(\"").append(param.getName()).append("\", this.").append(param.getFieldName()).append(")");
                        }
                    }
                    sb.append("\n");
                    sb.append("                    )\n");
                    sb.append("                ).addHeader(authHeader())\n");
                }
            }
            sb.append("            );\n");
            sb.append("            return transcoder().decodeFromString(response.returnContent().asString(), ").append(this.getReturnType().getSimpleName()).append(".class);\n");
            sb.append("        }\n");
            sb.append("        catch (IOException e)\n");
            sb.append("        {\n");
            sb.append("            throw new BergamotAPIException(\"Error calling Bergamot Monitoring API\", e);\n");
            sb.append("        }\n");
            sb.append("    }\n");
            sb.append("\n");
            //
            sb.append("}\n");
            return sb.toString();
        }
    }
    
    public static class RouteParameter
    {
        private final String name;
        
        private final String fieldName;
        
        private final Class<?> type;
        
        private final boolean as;
        
        public RouteParameter(String name, String fieldName, Class<?> type, boolean as)
        {
            this.name = name;
            this.fieldName = fieldName;
            this.type = type;
            this.as = as;
        }

        public String getName()
        {
            return name;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public Class<?> getType()
        {
            return type;
        }
        
        public boolean isAs()
        {
            return this.as;
        }
    }
}

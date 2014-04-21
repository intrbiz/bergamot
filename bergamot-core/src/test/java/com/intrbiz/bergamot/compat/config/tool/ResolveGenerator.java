package com.intrbiz.bergamot.compat.config.tool;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.compat.config.model.ConfigObject;

public class ResolveGenerator
{
    public static boolean containsMethod(Method[] methods, String name)
    {
        for (Method method : methods)
        {
            if (name.equals(method.getName())) return true;
        }
        return false;
    }
    
    public static String generateResolveMethods(Class<? extends ConfigObject<?>> model)
    {
        List<Method> getters = new LinkedList<Method>();
        // find all getters
        for (Method method : model.getDeclaredMethods())
        {
            if (method.getName().startsWith("get") && (! containsMethod(model.getDeclaredMethods(), "resolve" + method.getName().substring(3))) && (!"getObjectName".equals(method.getName())))
            {
                getters.add(method);
            }
        }
        //
        StringBuilder code = new StringBuilder();
        //
        for (Method getter : getters)
        {
            String returnType = getter.getReturnType().getSimpleName();
            if ("List".equals(returnType)) returnType = "List<String>";
            //
            code.append("\r\n");
            code.append("    public ").append(returnType).append(" resolve").append(getter.getName().substring(3)).append("()\r\n");
            code.append("    {\r\n");
            code.append("        return this.resolveProperty((p) -> { return p.").append(getter.getName()).append("(); });\r\n");
            code.append("    }\r\n");
        }
        //
        return code.toString();
    }
    
    public static void main(String[] args)
    {
        
    }
}

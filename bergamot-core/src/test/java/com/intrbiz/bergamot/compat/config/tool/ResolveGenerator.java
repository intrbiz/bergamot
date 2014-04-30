package com.intrbiz.bergamot.compat.config.tool;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.compat.config.model.CommandCfg;
import com.intrbiz.bergamot.compat.config.model.ConfigObject;
import com.intrbiz.bergamot.compat.config.model.ContactCfg;
import com.intrbiz.bergamot.compat.config.model.ContactgroupCfg;
import com.intrbiz.bergamot.compat.config.model.HostCfg;
import com.intrbiz.bergamot.compat.config.model.HostgroupCfg;
import com.intrbiz.bergamot.compat.config.model.LocationCfg;
import com.intrbiz.bergamot.compat.config.model.ServiceCfg;
import com.intrbiz.bergamot.compat.config.model.ServicegroupCfg;
import com.intrbiz.bergamot.compat.config.model.TimeperiodCfg;

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
            if ((method.getName().startsWith("get") || method.getName().startsWith("is")) && (! containsMethod(model.getDeclaredMethods(), "resolve" + method.getName().substring(method.getName().startsWith("is") ? 2 : 3))) && (!"getObjectName".equals(method.getName())))
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
            code.append("    public ").append(returnType).append(" resolve").append(getter.getName().substring(getter.getName().startsWith("is") ? 2 : 3)).append("()\r\n");
            code.append("    {\r\n");
            code.append("        return this.resolveProperty((p) -> { return p.").append(getter.getName()).append("(); });\r\n");
            code.append("    }\r\n");
        }
        //
        return code.toString();
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        for (Class<? extends ConfigObject<?>> cls : new Class[] { CommandCfg.class, ContactCfg.class, ContactgroupCfg.class, HostCfg.class, HostgroupCfg.class, LocationCfg.class, ServiceCfg.class, ServicegroupCfg.class, TimeperiodCfg.class })
        {
            System.out.println(cls.getName() + "\r\n");
            System.out.println(generateResolveMethods(cls));
            System.out.println();
        }
    }
}

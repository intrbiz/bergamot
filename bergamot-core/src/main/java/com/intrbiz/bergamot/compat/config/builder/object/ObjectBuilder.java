package com.intrbiz.bergamot.compat.config.builder.object;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;
import com.intrbiz.bergamot.compat.config.builder.parameter.BooleanParameterParser;
import com.intrbiz.bergamot.compat.config.builder.parameter.FloatParameterParser;
import com.intrbiz.bergamot.compat.config.builder.parameter.IntParameterParser;
import com.intrbiz.bergamot.compat.config.builder.parameter.ListParameterParser;
import com.intrbiz.bergamot.compat.config.builder.parameter.LongParameterParser;
import com.intrbiz.bergamot.compat.config.builder.parameter.ParameterParser;
import com.intrbiz.bergamot.compat.config.builder.parameter.StringParameterParser;
import com.intrbiz.bergamot.compat.config.model.ConfigObject;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectDefinition;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectParameter;

public class ObjectBuilder
{
    private Logger logger = Logger.getLogger(ObjectBuilder.class);
    
    private final Class<? extends ConfigObject<?>> typeClass;
    
    private final String type;
    
    private Map<String, ParameterParser> parameters = new TreeMap<String, ParameterParser>();
    
    public ObjectBuilder(Class<? extends ConfigObject<?>> typeClass, String type)
    {
        super();
        this.typeClass = typeClass;
        this.type = type;
    }

    public Class<? extends ConfigObject<?>> getTypeClass()
    {
        return typeClass;
    }

    public String getType()
    {
        return type;
    }
    
    public Set<String> getParameterNames()
    {
        return this.parameters.keySet();
    }
    
    public void addParameter(ParameterParser parser)
    {
        this.parameters.put(parser.getName(), parser);
        logger.trace("Adding parameter " + parser.getName() + " to object " + this.getType());
    }
    
    public ConfigObject<?> build(ObjectDefinition def)
    {
        try
        {
            ConfigObject<?> obj = this.typeClass.newInstance();
            for (ObjectParameter param : def.getParameters())
            {
                ParameterParser pp = this.parameters.get(param.getName());
                if (pp != null)
                {
                    pp.build(obj, param);
                }
                else if (! obj.unhandledObjectParameter(param))
                {
                    logger.warn("Unsupported parameter " + param.getName() + " of object " + this.getType() + ", ignoring it");
                }
            }
            return obj;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error building " + this.getType() + " object", e);
        }
    }

    public static ObjectBuilder create(Class<? extends ConfigObject<?>> typeClass)
    {
        TypeName typeName = typeClass.getAnnotation(TypeName.class);
        ObjectBuilder builder = new ObjectBuilder(typeClass, typeName.value());
        for (Method method : typeClass.getMethods())
        {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1)
            {
                ParameterName paramName = method.getAnnotation(ParameterName.class);
                if (paramName != null)
                {
                    Class<?> paramType = method.getParameterTypes()[0];
                    // select a parser based on the parameter type
                    if (paramType == String.class)
                    {
                        builder.addParameter(new StringParameterParser(paramName.value(), paramType, method));
                    }
                    else if (paramType == Long.class)
                    {
                        builder.addParameter(new LongParameterParser(paramName.value(), paramType, method));
                    }
                    else if (paramType == Integer.class)
                    {
                        builder.addParameter(new IntParameterParser(paramName.value(), paramType, method));
                    }
                    else if (paramType == Float.class)
                    {
                        builder.addParameter(new FloatParameterParser(paramName.value(), paramType, method));
                    }
                    else if (List.class.isAssignableFrom(paramType))
                    {
                        builder.addParameter(new ListParameterParser(paramName.value(), paramType, method));
                    }
                    else if (paramType == Boolean.class)
                    {
                        builder.addParameter(new BooleanParameterParser(paramName.value(), paramType, method));
                    }
                }
            }
        }
        return builder;
    }
}

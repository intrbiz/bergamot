package com.intrbiz.bergamot.compat.config.builder.parameter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.intrbiz.bergamot.compat.config.model.ConfigObject;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectParameter;

public abstract class ParameterParser
{
    private final String name;
    
    private final Class<?> type;
    
    private final Method method;
    
    public ParameterParser(String name, Class<?> type, Method method)
    {
        this.name = name;
        this.type = type;
        this.method = method;
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getType()
    {
        return type;
    }

    public Method getMethod()
    {
        return method;
    }
    
    public void build(ConfigObject<?> object, ObjectParameter value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Object convertedValue = this.convert(value.getValue());
        // set the value
        this.method.invoke(object, new Object[] { convertedValue });
    }
    
    protected abstract Object convert(String value);
}

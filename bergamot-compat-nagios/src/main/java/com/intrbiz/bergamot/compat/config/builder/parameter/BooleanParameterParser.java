package com.intrbiz.bergamot.compat.config.builder.parameter;

import java.lang.reflect.Method;

public class BooleanParameterParser extends ParameterParser
{

    public BooleanParameterParser(String name, Class<?> type, Method method)
    {
        super(name, type, method);
    }

    @Override
    protected Object convert(String value)
    {
        return new Boolean("1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value));
    }
}

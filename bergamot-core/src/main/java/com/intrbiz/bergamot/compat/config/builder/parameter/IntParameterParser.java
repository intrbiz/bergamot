package com.intrbiz.bergamot.compat.config.builder.parameter;

import java.lang.reflect.Method;

public class IntParameterParser extends ParameterParser
{

    public IntParameterParser(String name, Class<?> type, Method method)
    {
        super(name, type, method);
    }

    @Override
    protected Object convert(String value)
    {
        return Integer.parseInt(value.trim());
    }
}

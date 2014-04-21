package com.intrbiz.bergamot.compat.config.builder.parameter;

import java.lang.reflect.Method;

public class FloatParameterParser extends ParameterParser
{

    public FloatParameterParser(String name, Class<?> type, Method method)
    {
        super(name, type, method);
    }

    @Override
    protected Object convert(String value)
    {
        return Float.parseFloat(value.trim());
    }
}

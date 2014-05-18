package com.intrbiz.bergamot.compat.config.builder.parameter;

import java.lang.reflect.Method;

public class LongParameterParser extends ParameterParser
{

    public LongParameterParser(String name, Class<?> type, Method method)
    {
        super(name, type, method);
    }

    @Override
    protected Object convert(String value)
    {
        return Long.parseLong(value.trim());
    }
}

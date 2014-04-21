package com.intrbiz.bergamot.compat.config.builder.parameter;

import java.lang.reflect.Method;

public class StringParameterParser extends ParameterParser
{

    public StringParameterParser(String name, Class<?> type, Method method)
    {
        super(name, type, method);
    }

    @Override
    protected Object convert(String value)
    {
        return value;
    }
}

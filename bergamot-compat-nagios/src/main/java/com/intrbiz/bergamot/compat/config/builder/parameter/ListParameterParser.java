package com.intrbiz.bergamot.compat.config.builder.parameter;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class ListParameterParser extends ParameterParser
{

    public ListParameterParser(String name, Class<?> type, Method method)
    {
        super(name, type, method);
    }

    @Override
    protected Object convert(String value)
    {
        List<String> values = new LinkedList<String>();
        for (String val : value.split(","))
        {
            if (val != null)
            {
                val = val.trim();
                values.add(val);
            }
        }
        return values;
    }
}

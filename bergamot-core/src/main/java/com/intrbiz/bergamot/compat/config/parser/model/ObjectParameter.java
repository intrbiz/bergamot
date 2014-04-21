package com.intrbiz.bergamot.compat.config.parser.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectParameter extends Directive
{
    private static final Pattern parameter = Pattern.compile("([a-zA-Z0-9_-]+)[\t ]+(.+)");

    private final String name;

    private final String value;

    public ObjectParameter(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public static ObjectParameter read(String l)
    {
        Matcher m = parameter.matcher(l);
        if (m.matches())
        {
            String name = m.group(1).trim();
            String value = m.group(2).trim();
            return new ObjectParameter(name, value);
        }
        return null;
    }
}
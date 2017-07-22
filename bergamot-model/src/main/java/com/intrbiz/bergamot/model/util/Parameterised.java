package com.intrbiz.bergamot.model.util;

import java.util.LinkedHashMap;

public interface Parameterised
{
    LinkedHashMap<String, Parameter> getParameters();

    void setParameters(LinkedHashMap<String, Parameter> parameters);

    default void addParameter(String name, String value)
    {
        this.getParameters().put(name, new Parameter(name, value));
    }

    default void setParameter(String name, String value)
    {
        this.getParameters().put(name, new Parameter(name, value));
    }

    default void removeParameter(String name)
    {
        this.getParameters().remove(name);
    }

    default void clearParameters()
    {
        this.getParameters().clear();
    }

    default String getParameter(String name)
    {
        Parameter parameter = this.getParameters().get(name);
        return parameter == null ? null : parameter.getValue();
    }

    default String getParameter(String name, String defaultValue)
    {
        Parameter parameter = this.getParameters().get(name);
        return parameter == null ? defaultValue : parameter.getValue();
    }
    
    default int getIntParameter(String name, int defaultValue)
    {
        Parameter parameter = this.getParameters().get(name);
        return parameter == null ? defaultValue : Integer.parseInt(parameter.getValue());
    }
    
    default long getLongParameter(String name, long defaultValue)
    {
        Parameter parameter = this.getParameters().get(name);
        return parameter == null ? defaultValue : Long.parseLong(parameter.getValue());
    }
    
    default float getFloatParameter(String name, float defaultValue)
    {
        Parameter parameter = this.getParameters().get(name);
        return parameter == null ? defaultValue : Float.parseFloat(parameter.getValue());
    }
    
    default double getDoubleParameter(String name, double defaultValue)
    {
        Parameter parameter = this.getParameters().get(name);
        return parameter == null ? defaultValue : Double.parseDouble(parameter.getValue());
    }
}

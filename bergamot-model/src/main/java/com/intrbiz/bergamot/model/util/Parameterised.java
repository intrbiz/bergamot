package com.intrbiz.bergamot.model.util;

import java.util.List;

public interface Parameterised
{
    List<Parameter> getParameters();

    void setParameters(List<Parameter> parameters);

    default void addParameter(String name, String value)
    {
        this.getParameters().add(new Parameter(name, value));
    }

    default void setParameter(String name, String value)
    {
        this.removeParameter(name);
        this.addParameter(name, value);
    }

    default void removeParameter(String name)
    {
        this.getParameters().removeIf((p) -> {return name.equals(p.getName());});
    }

    default void clearParameters()
    {
        this.getParameters().clear();
    }

    default String getParameter(String name)
    {
        return this.getParameter(name, null);
    }

    default String getParameter(String name, String defaultValue)
    {
        return this.getParameters().stream()
                .filter((p) -> {return name.equals(p.getName());})
                .findFirst()
                .map(Parameter::getValue)
                .orElse(defaultValue);
    }
}

package com.intrbiz.bergamot.model.util;

import java.util.List;

public interface Parameterised
{
    List<Parameter> getParameters();

    void setParameters(List<Parameter> parameters);

    void addParameter(String name, String value);

    void setParameter(String name, String value);

    void removeParameter(String name);

    void clearParameters();

    String getParameter(String name);

    String getParameter(String name, String defaultValue);
}

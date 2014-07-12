package com.intrbiz.bergamot.model.message;

import java.util.Iterator;
import java.util.List;

public interface ParameterisedMO
{
    List<ParameterMO> getParameters();

    void setParameters(List<ParameterMO> parameters);

    default void addParameter(String name, String value)
    {
        this.getParameters().add(new ParameterMO(name, value));
    }

    default void setParameter(String name, String value)
    {
        this.removeParameter(name);
        this.addParameter(name, value);
    }

    default void removeParameter(String name)
    {
        for (Iterator<ParameterMO> i = this.getParameters().iterator(); i.hasNext();)
        {
            if (name.equals(i.next().getName()))
            {
                i.remove();
                break;
            }
        }
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
        for (ParameterMO parameter : this.getParameters())
        {
            if (name.equals(parameter.getName())) return parameter.getValue();
        }
        return defaultValue;
    }
}

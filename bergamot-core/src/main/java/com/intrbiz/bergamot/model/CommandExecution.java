package com.intrbiz.bergamot.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.model.util.Parameterised;

public class CommandExecution implements Parameterised
{
    private Command command;

    private List<Parameter> parameters = new LinkedList<Parameter>();

    public CommandExecution()
    {
        super();
    }

    public Command getCommand()
    {
        return command;
    }

    public void setCommand(Command command)
    {
        this.command = command;
    }

    @Override
    public List<Parameter> getParameters()
    {
        return parameters;
    }

    @Override
    public void setParameters(List<Parameter> parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public void addParameter(String name, String value)
    {
        this.parameters.add(new Parameter(name, value));
    }

    @Override
    public void setParameter(String name, String value)
    {
        this.removeParameter(name);
        this.addParameter(name, value);
    }

    @Override
    public void removeParameter(String name)
    {
        for (Iterator<Parameter> i = this.parameters.iterator(); i.hasNext();)
        {
            if (name.equals(i.next().getName()))
            {
                i.remove();
                break;
            }
        }
    }

    @Override
    public void clearParameters()
    {
        this.parameters.clear();
    }

    @Override
    public String getParameter(String name)
    {
        return this.getParameter(name, null);
    }

    @Override
    public String getParameter(String name, String defaultValue)
    {
        for (Parameter parameter : this.parameters)
        {
            if (name.equals(parameter.getName())) return parameter.getValue();
        }
        return defaultValue;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.command.getEngine()).append("::").append(this.command.getName()).append("(");
        boolean ns = false;
        for (Parameter param : this.parameters)
        {
            if (ns) sb.append(", ");
            sb.append(param.getName()).append(" => ").append(param.getValue());
            ns = true;
        }
        sb.append(")");
        return sb.toString();
    }
}

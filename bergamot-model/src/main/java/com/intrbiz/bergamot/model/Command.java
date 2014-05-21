package com.intrbiz.bergamot.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.model.util.Parameterised;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.configuration.Configurable;

/**
 * The definition of a command which is used to check something
 */
public class Command extends NamedObject<CommandMO> implements Parameterised, Configurable<CommandCfg>
{
    private String engine;

    private List<Parameter> parameters = new LinkedList<Parameter>();

    private CommandCfg config;

    public Command()
    {
        super();
    }

    @Override
    public void configure(CommandCfg cfg)
    {
        this.config = cfg;
        // resolve
        CommandCfg rcfg = cfg.resolve();
        this.engine = rcfg.getEngine();
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        // load the parameters
        for (CfgParameter cp : rcfg.getParameters())
        {
            System.out.println("Adding parameter: " + cp.getName() + " => " + cp.getText());
            this.addParameter(cp.getName(), cp.getText());
        }
    }

    @Override
    public CommandCfg getConfiguration()
    {
        return this.config;
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((engine == null) ? 0 : engine.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Command other = (Command) obj;
        if (engine == null)
        {
            if (other.engine != null) return false;
        }
        else if (!engine.equals(other.engine)) return false;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
    
    @Override
    public CommandMO toMO(boolean stub)
    {
        CommandMO mo = new CommandMO();
        super.toMO(mo, stub);
        mo.setEngine(this.getEngine());
        mo.setParameters(this.getParameters().stream().map(Parameter::toMO).collect(Collectors.toList()));
        return mo;
    }
}

package com.intrbiz.bergamot.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.CommandCfgAdapter;
import com.intrbiz.bergamot.model.adapter.ParametersAdapter;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * The definition of a command which is used to check something
 */
@SQLTable(schema = BergamotDB.class, name = "command", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class Command extends NamedObject<CommandMO, CommandCfg>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "configuration", type = "TEXT", adapter = CommandCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected CommandCfg configuration;

    @SQLColumn(index = 2, name = "engine", since = @SQLVersion({ 1, 0, 0 }))
    private String engine;

    @SQLColumn(index = 3, name = "executor", since = @SQLVersion({ 1, 0, 0 }))
    private String executor;

    @SQLColumn(index = 4, name = "parameters", type = "JSON", adapter = ParametersAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    private List<Parameter> parameters = new LinkedList<Parameter>();

    public Command()
    {
        super();
    }

    @Override
    public CommandCfg getConfiguration()
    {
        return configuration;
    }

    @Override
    public void setConfiguration(CommandCfg configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void configure(CommandCfg cfg)
    {
        super.configure(cfg);
        // resolve
        CommandCfg rcfg = cfg.resolve();
        this.engine = rcfg.getEngine();
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
        // load the parameters
        this.clearParameters();
        for (CfgParameter cp : rcfg.getParameters())
        {
            this.addParameter(cp.getName(), cp.getValueOrText());
        }
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public List<Parameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters)
    {
        this.parameters = parameters;
    }

    public void addParameter(String name, String value)
    {
        this.parameters.add(new Parameter(name, value));
    }

    public void setParameter(String name, String value)
    {
        this.removeParameter(name);
        this.addParameter(name, value);
    }

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

    public void clearParameters()
    {
        this.parameters.clear();
    }

    public String getParameter(String name)
    {
        return this.getParameter(name, null);
    }

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
    
    public String toString()
    {
        return "CheckCommand { name => " + this.getName() + ", engine =>" + this.getEngine() + ", executor => " + this.executor + ", parameters => " + this.getParameters() + "}"; 
    }
}
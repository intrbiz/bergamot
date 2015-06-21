package com.intrbiz.bergamot.model;

import java.util.stream.Collectors;

import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.CommandMO;
import com.intrbiz.bergamot.model.util.Parameter;
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

    @SQLColumn(index = 1, name = "engine", since = @SQLVersion({ 1, 0, 0 }))
    private String engine;

    @SQLColumn(index = 2, name = "executor", since = @SQLVersion({ 1, 0, 0 }))
    private String executor;
    
    @SQLColumn(index = 3, name = "category", since = @SQLVersion({ 2, 5, 0 }))
    private String category;

    @SQLColumn(index = 4, name = "application", since = @SQLVersion({ 2, 5, 0 }))
    private String application;

    public Command()
    {
        super();
    }

    @Override
    public void configure(CommandCfg configuration, CommandCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        this.engine   = resolvedConfiguration.getEngine();
        this.executor = resolvedConfiguration.getExecutor();
        this.category = resolvedConfiguration.getCategory();
        this.application = resolvedConfiguration.getApplication();
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

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
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

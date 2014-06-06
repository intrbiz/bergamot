package com.intrbiz.bergamot.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.CheckCommandCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.CheckCommandCfgAdapter;
import com.intrbiz.bergamot.model.adapter.ParametersAdapter;
import com.intrbiz.bergamot.model.message.CheckCommandMO;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * The definition of a command which is used to check something
 */
@SQLTable(schema = BergamotDB.class, name = "check_command", since = @SQLVersion({ 1, 0, 0 }))
public class CheckCommand extends BergamotObject<CheckCommandMO> implements Configurable<CheckCommandCfg>
{
    @SQLColumn(index = 1, name = "check_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID checkId;

    @SQLColumn(index = 2, name = "configuration", type = "TEXT", adapter = CheckCommandCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected CheckCommandCfg configuration;

    @SQLColumn(index = 3, name = "command_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID commandId;

    @SQLColumn(index = 4, name = "parameters", type = "JSON", adapter = ParametersAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    private List<Parameter> parameters = new LinkedList<Parameter>();

    public CheckCommand()
    {
        super();
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public UUID getCommandId()
    {
        return commandId;
    }

    public void setCommandId(UUID commandId)
    {
        this.commandId = commandId;
    }
    
    public Command getCommand()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCommand(this.getCommandId());
        }
    }

    @Override
    public CheckCommandCfg getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(CheckCommandCfg configuration)
    {
        this.configuration = configuration;
    }

    public void configure(CheckCommandCfg cfg)
    {
        // load the parameters
        this.clearParameters();
        for (CfgParameter cp : cfg.getParameters())
        {
            this.addParameter(cp.getName(), cp.getValueOrText());
        }
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
    
    /**
     * Resolve the parameters between the command and this check definition
     * @return
     */
    public List<Parameter> resolveCheckParameters()
    {
        List<Parameter> r = new LinkedList<Parameter>();
        Command command = this.getCommand();
        if (this.getParameters() != null)
        {
            for (Parameter e : this.getParameters())
            {
                if (! r.contains(e)) r.add(e);
            }
        }
        if (command != null && command.getParameters() != null)
        {
            for (Parameter e : command.getParameters())
            {
                if (! r.contains(e)) r.add(e);
            }
        }
        return r;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((checkId == null) ? 0 : checkId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CheckCommand other = (CheckCommand) obj;
        if (checkId == null)
        {
            if (other.checkId != null) return false;
        }
        else if (!checkId.equals(other.checkId)) return false;
        return true;
    }

    @Override
    public CheckCommandMO toMO(boolean stub)
    {
        CheckCommandMO mo = new CheckCommandMO();
        mo.setCommand(Util.nullable(this.getCommand(), Command::toStubMO));
        mo.setParameters(this.getParameters().stream().map(Parameter::toMO).collect(Collectors.toList()));
        return mo;
    }
    
    public String toString()
    {
        return "CheckCommand { command => " + this.getCommand() +  ", parameters => " + this.getParameters() + "}"; 
    }
}

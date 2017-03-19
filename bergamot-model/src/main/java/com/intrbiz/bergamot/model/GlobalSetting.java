package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.ParametersAdapter;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.model.util.Parameterised;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * Global system settings
 */
@SQLTable(schema = BergamotDB.class, name = "global_setting", since = @SQLVersion({ 3, 44, 0 }))
public final class GlobalSetting implements Serializable, Parameterised
{
    public static final class NAME
    {
        public static final String FIRST_INSTALL = "first.install";
        public static final String GLOBAL_ADMINS = "global.admins";
    }
    
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "name", notNull = true, since = @SQLVersion({ 3, 44, 0 }))
    @SQLPrimaryKey()
    protected String name;

    @SQLColumn(index = 2, name = "value", since = @SQLVersion({ 3, 44, 0 }))
    protected String value;

    /**
     * Arbitrary parameters of an object
     */
    @SQLColumn(index = 3, name = "parameters", type = "JSON", adapter = ParametersAdapter.class, since = @SQLVersion({ 3, 44, 0 }))
    private LinkedHashMap<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();

    public GlobalSetting()
    {
        super();
    }
    
    public GlobalSetting(String name)
    {
        super();
        this.name = name;
    }

    public GlobalSetting(String name, String value)
    {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public LinkedHashMap<String, Parameter> getParameters()
    {
        return parameters;
    }

    @Override
    public void setParameters(LinkedHashMap<String, Parameter> parameters)
    {
        if (parameters == null) parameters = new LinkedHashMap<String, Parameter>();
        this.parameters = parameters;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GlobalSetting other = (GlobalSetting) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}

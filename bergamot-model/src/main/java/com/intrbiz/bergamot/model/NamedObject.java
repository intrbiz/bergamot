package com.intrbiz.bergamot.model;

import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.NamedObjectCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.ParametersAdapter;
import com.intrbiz.bergamot.model.message.NamedObjectMO;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.model.util.Parameterised;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A generic object with an id and a name
 */
public abstract class NamedObject<T extends NamedObjectMO, C extends NamedObjectCfg<C>> extends BergamotObject<T> implements Parameterised
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    protected UUID id;

    @SQLColumn(index = 2, name = "site_id", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    protected UUID siteId;

    @SQLColumn(index = 3, name = "name", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String name;

    @SQLColumn(index = 4, name = "summary", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    @SQLColumn(index = 5, name = "description", since = @SQLVersion({ 1, 0, 0 }))
    protected String description;

    @SQLColumn(index = 6, name = "created", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp created = new Timestamp(System.currentTimeMillis());

    @SQLColumn(index = 7, name = "updated", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp updated = new Timestamp(System.currentTimeMillis());
    
    /**
     * Arbitrary parameters of an object
     */
    @SQLColumn(index = 8, name = "parameters", type = "JSON", adapter = ParametersAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    private List<Parameter> parameters = new LinkedList<Parameter>();

    public NamedObject()
    {
        super();
    }
    
    public final void configure(C configuration)
    {
        this.configure(configuration, configuration.resolve());
    }

    public void configure(C configuration, C resolvedConfiguration)
    {
        // ids and site
        this.setId(configuration.getId());
        this.setSiteId(Site.getSiteId(this.getId()));
        // naming
        this.name        = resolvedConfiguration.getName();
        this.summary     = Util.coalesceEmpty(resolvedConfiguration.getSummary(), this.name);
        this.description = Util.coalesceEmpty(resolvedConfiguration.getDescription(), "");
        // copy the parameters
        this.parameters.clear();
        for (CfgParameter param : resolvedConfiguration.getParameters())
        {
            this.parameters.add(new Parameter(param.getName(), param.getDescription(), param.getValueOrText()));
        }
        // store the config
        this.setConfiguration(configuration);
    }
    
    @SuppressWarnings("unchecked")
    public final C getConfiguration()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return (C) Util.nullable(db.getConfig(this.getId()), Config::getConfiguration);
        }
    }
    
    public final void setConfiguration(C config)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.setConfig(new Config(this.getId(), this.getSiteId(), config));
        }
    }

    public final UUID getId()
    {
        return id;
    }

    public final void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public Site getSite()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getSite(this.getSiteId());
        }
    }

    public final String getName()
    {
        return name;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    public final String getSummary()
    {
        return summary;
    }

    public final void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public Timestamp getUpdated()
    {
        return updated;
    }

    public void setUpdated(Timestamp updated)
    {
        this.updated = updated;
    }
    
    @Override
    public List<Parameter> getParameters()
    {
        return parameters;
    }

    @Override
    public void setParameters(List<Parameter> parameters)
    {
        if (parameters == null) parameters = new LinkedList<Parameter>();
        this.parameters = parameters;
    }

    protected void toMO(NamedObjectMO mo, Contact contact, EnumSet<MOFlag> options)
    {
        mo.setId(this.getId());
        mo.setSiteId(this.getSiteId());
        mo.setName(this.getName());
        mo.setSummary(this.getSummary());
        if (options.contains(MOFlag.DESCRIPTION)) mo.setDescription(this.getDescription());
        if (options.contains(MOFlag.PARAMETERS)) mo.setParameters(this.getParameters().stream().map((x) -> x.toMO(contact)).collect(Collectors.toList()));
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NamedObject<?, ?> other = (NamedObject<?, ?>) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }
}

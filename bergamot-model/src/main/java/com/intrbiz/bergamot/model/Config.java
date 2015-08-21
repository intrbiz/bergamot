package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.NamedObjectCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.BergamotCfgAdapter;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * An alert which was raised against a check
 */
@SQLTable(schema = BergamotDB.class, name = "config", since = @SQLVersion({ 1, 0, 0 }))
public class Config implements Serializable
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    protected UUID id;

    @SQLColumn(index = 2, name = "site_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    protected UUID siteId;

    @SQLColumn(index = 3, name = "type", since = @SQLVersion({ 1, 0, 0 }))
    protected String type;

    @SQLColumn(index = 4, name = "name", since = @SQLVersion({ 1, 0, 0 }))
    protected String name;

    @SQLColumn(index = 5, name = "template", since = @SQLVersion({ 1, 0, 0 }))
    protected boolean template;

    @SQLColumn(index = 6, name = "inherits", type = "TEXT[]", since = @SQLVersion({ 1, 0, 0 }))
    protected List<String> inherits = new LinkedList<String>();

    @SQLColumn(index = 7, name = "summary", since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    @SQLColumn(index = 8, name = "description", since = @SQLVersion({ 1, 0, 0 }))
    protected String description;

    @SQLColumn(index = 9, name = "configuration", type = "TEXT", adapter = BergamotCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected Configuration configuration;

    @SQLColumn(index = 10, name = "required_templates", type = "TEXT[]", since = @SQLVersion({ 1, 0, 0 }))
    protected List<String> requiredTemplates = new LinkedList<String>();

    public Config()
    {
        super();
    }

    public Config(UUID id, UUID siteId, NamedObjectCfg<?> configuration)
    {
        super();
        this.id = id;
        this.siteId = siteId;
        this.fromConfiguration(configuration);
    }

    public Config fromConfiguration(NamedObjectCfg<?> configuration)
    {
        this.type = Configuration.getRootElement(configuration.getClass());
        this.name = configuration.getName();
        this.template = configuration.getTemplateBooleanValue();
        this.inherits = new LinkedList<String>(configuration.getInheritedTemplates());
        this.summary = Util.coalesceEmpty(configuration.getSummary(), Util.ucFirst(this.name));
        this.description = configuration.getDescription();
        this.configuration = configuration;
        // compute the templates used by this configuration
        Set<String> requireTemplateSet = new HashSet<String>();
        for (String inherit : this.inherits)
        {
            requireTemplateSet.add(getObjectQualifiedName(this.type, inherit));
        }
        for (TemplatedObjectCfg<?> child : configuration.getTemplatedChildObjects())
        {
            for (String inherit : child.getInheritedTemplates())
            {
                requireTemplateSet.add(getObjectQualifiedName(Configuration.getRootElement(child.getClass()), inherit));    
            }
        }
        this.requiredTemplates = new LinkedList<String>(requireTemplateSet);
        return this;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getQualifiedName()
    {
        return this.getType() + ":" + this.getName();
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
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

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Configuration getResolvedConfiguration()
    {
        if (this.configuration != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                db.getConfigResolver(this.siteId).computeInheritenance((TemplatedObjectCfg) this.configuration);
            }
        }
        return configuration;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public boolean isTemplate()
    {
        return template;
    }

    public void setTemplate(boolean template)
    {
        this.template = template;
    }

    public List<String> getInherits()
    {
        return inherits;
    }

    public void setInherits(List<String> inherits)
    {
        this.inherits = inherits;
    }

    public List<String> getRequiredTemplates()
    {
        return requiredTemplates;
    }

    public void setRequiredTemplates(List<String> requiredTemplates)
    {
        this.requiredTemplates = requiredTemplates;
    }
    
    public List<Config> listDependents()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.listDependentConfig(this.getSiteId(), getObjectQualifiedName(this.getType(), this.getName()));
        }
    }
    
    public List<Config> listAllDependentsObjects()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.listAllDependentConfigObjects(this.getSiteId(), getObjectQualifiedName(this.getType(), this.getName()));
        }
    }

    public static final String getObjectQualifiedName(NamedObjectCfg<?> object)
    {
        if (object == null || object.getName() == null) throw new NullPointerException("Object is null or has no name!");
        return Configuration.getRootElement(object.getClass()) + ":" + object.getName();
    }

    public static final String getObjectQualifiedName(String type, String name)
    {
        return type + ":" + name;
    }
}

package com.intrbiz.bergamot.model;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.NamedObjectCfg;
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
@SQLTable(schema = BergamotDB.class, name = "config_template", since = @SQLVersion({ 1, 0, 0 }))
public class ConfigTemplate
{
    /**
     * The site id
     */
    @SQLColumn(index = 1, name = "site_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT)
    @SQLPrimaryKey()
    protected UUID siteId;

    @SQLColumn(index = 2, name = "type", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    protected String type;

    @SQLColumn(index = 3, name = "name", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    protected String name;

    @SQLColumn(index = 4, name = "summary", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    @SQLColumn(index = 5, name = "description", since = @SQLVersion({ 1, 0, 0 }))
    protected String description;

    @SQLColumn(index = 6, name = "configuration", type = "TEXT", adapter = BergamotCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected Configuration configuration;

    public ConfigTemplate()
    {
        super();
    }

    public ConfigTemplate(UUID siteId, NamedObjectCfg<?> configuration)
    {
        super();
        this.siteId = siteId;
        this.type = Configuration.getRootElement(configuration.getClass());
        this.name = configuration.getName();
        this.summary = Util.coalesceEmpty(configuration.getSummary(), Util.ucFirst(this.name));
        this.description = configuration.getDescription();
        this.configuration = configuration;
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
}

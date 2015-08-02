package com.intrbiz.bergamot.config.model;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeListUnique;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.util.uuid.UUIDAdapter;

public abstract class NamedObjectCfg<P extends NamedObjectCfg<P>> extends TemplatedObjectCfg<P>
{
    private static final long serialVersionUID = 1L;

    private UUID id = null;

    private String summary;

    private String description;
    
    private List<TagCfg> tags = new LinkedList<TagCfg>();

    public NamedObjectCfg()
    {
        super();
    }

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(UUIDAdapter.class)
    @ResolveWith(Coalesce.class)
    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    @XmlElement(name = "summary")
    @ResolveWith(CoalesceEmptyString.class)
    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    @XmlElement(name = "description")
    @ResolveWith(CoalesceEmptyString.class)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @XmlElementRef(type = TagCfg.class)
    @ResolveWith(MergeListUnique.class)
    public List<TagCfg> getTags()
    {
        return tags;
    }

    public void setTags(List<TagCfg> tags)
    {
        this.tags = tags;
    }
    
    // override to specify the resolver
    
    @Override
    @ResolveWith(CoalesceEmptyString.class)
    public String getName()
    {
        return super.getName();
    }

    @Override
    @ResolveWith(MergeListUnique.class)
    public List<CfgParameter> getParameters()
    {
        return super.getParameters();
    }
}

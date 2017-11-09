package com.intrbiz.bergamot.config.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

public abstract class SLAPeriodCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    private String summary;
    
    private String description;

    public SLAPeriodCfg()
    {
        super();
    }

    @XmlAttribute(name = "name")
    @ResolveWith(CoalesceEmptyString.class)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
        SLAPeriodCfg other = (SLAPeriodCfg) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}

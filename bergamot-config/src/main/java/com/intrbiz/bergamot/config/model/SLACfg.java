package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeListUnique;

@XmlType(name = "sla")
@XmlRootElement(name = "sla")
public class SLACfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String target;
    
    private String name;
    
    private String summary;
    
    private String description;
    
    private List<SLAPeriodCfg> periods = new LinkedList<SLAPeriodCfg>();

    public SLACfg()
    {
        super();
    }

    @XmlAttribute(name = "target")
    @ResolveWith(CoalesceEmptyString.class)
    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }
    
    /**
     * Parse the target to a number between 0 and 1
     * @return the normalised target or < 0 if the target is undefined
     */
    public float getTargetValue()
    {
        String target = this.getTarget();
        if (! Util.isEmpty(target))
        {
            target = target.trim().replace("%", "");
            float value = Float.parseFloat(target) / 100F;
            // validate the value
            if (0F <= value && value <= 1F)
                return value;
        }
        return -1F;
    }

    @XmlElementRefs({
        @XmlElementRef(type = SLARollingPeriodCfg.class),
        @XmlElementRef(type = SLAFixedPeriodCfg.class)
    })
    @ResolveWith(MergeListUnique.class)
    public List<SLAPeriodCfg> getPeriods()
    {
        return periods;
    }

    public void setPeriods(List<SLAPeriodCfg> periods)
    {
        this.periods = periods;
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
        SLACfg other = (SLACfg) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}

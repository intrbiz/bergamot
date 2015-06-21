package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "service")
@XmlRootElement(name = "service")
public class ServiceCfg extends ActiveCheckCfg<ServiceCfg>
{
    private static final long serialVersionUID = 1L;
    
    private String category;
    
    private String application;
    
    public ServiceCfg()
    {
        super();
    }
    
    @XmlAttribute(name = "category")
    @ResolveWith(CoalesceEmptyString.class)
    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    @XmlAttribute(name = "application")
    @ResolveWith(CoalesceEmptyString.class)
    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }
}

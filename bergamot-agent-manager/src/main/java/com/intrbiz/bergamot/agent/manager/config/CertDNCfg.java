package com.intrbiz.bergamot.agent.manager.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "certificate-name")
@XmlRootElement(name = "certificate-name")
public class CertDNCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String country;
    
    private String state;
    
    private String locality;
    
    private String organisation;
    
    public CertDNCfg()
    {
        super();
    }

    @XmlAttribute(name = "country")
    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    @XmlAttribute(name = "state")
    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    @XmlAttribute(name = "locality")
    public String getLocality()
    {
        return locality;
    }

    public void setLocality(String locality)
    {
        this.locality = locality;
    }

    @XmlAttribute(name = "organisation")
    public String getOrganisation()
    {
        return organisation;
    }

    public void setOrganisation(String organisation)
    {
        this.organisation = organisation;
    }
}

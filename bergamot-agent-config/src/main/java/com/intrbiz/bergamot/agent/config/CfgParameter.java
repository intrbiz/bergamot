package com.intrbiz.bergamot.agent.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * An arbitrary name value pair of configuration data
 */
@XmlType(name = "parameter")
@XmlRootElement(name = "parameter")
public class CfgParameter implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private String value;

    private String text;

    public CfgParameter()
    {
    }

    public CfgParameter(String name, String value)
    {
        super();
        this.name = name;
        this.value = value;
    }

    public CfgParameter(String name, String description, String value)
    {
        super();
        this.name = name;
        this.description = description;
        this.value = value;
    }
    
    public CfgParameter(String name, String description, String value, String text)
    {
        super();
        this.name = name;
        this.description = description;
        this.value = value;
        this.text = text;
    }

    @XmlAttribute(name = "name")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @XmlAttribute(name = "description")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @XmlAttribute(name = "value")
    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @XmlValue()
    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
    
    public String getValueOrText()
    {
        return (this.value == null || this.value.trim().length() == 0) ? this.value : this.text;
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
        CfgParameter other = (CfgParameter) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}

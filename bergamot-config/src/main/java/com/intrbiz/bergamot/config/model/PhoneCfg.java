package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "phone")
@XmlRootElement(name = "phone")
public class PhoneCfg
{
    private String type = "work";
    
    private String number;
    
    public PhoneCfg()
    {
        super();
    }
    
    public PhoneCfg(String type, String number)
    {
        super();
        this.type = type;
        this.number = number;
    }

    @XmlAttribute(name="type")
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @XmlValue
    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PhoneCfg other = (PhoneCfg) obj;
        if (number == null)
        {
            if (other.number != null) return false;
        }
        else if (!number.equals(other.number)) return false;
        if (type == null)
        {
            if (other.type != null) return false;
        }
        else if (!type.equals(other.type)) return false;
        return true;
    }
}

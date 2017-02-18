package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "logging")
@XmlRootElement(name = "logging")
public class LoggingCfg
{
    private String level = "info";
    
    public LoggingCfg()
    {
        super();
    }

    @XmlAttribute(name = "level")
    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }
}

package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "initially")
@XmlRootElement(name = "initially")
public class InitiallyCfg
{
    private String status;

    private String output;

    public InitiallyCfg()
    {
        super();
    }

    @XmlAttribute(name = "status")
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @XmlAttribute(name = "output")
    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }
}

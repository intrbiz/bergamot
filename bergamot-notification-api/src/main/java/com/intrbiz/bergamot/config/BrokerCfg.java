package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "broker")
@XmlRootElement(name = "broker")
public class BrokerCfg
{
    private String url;
    
    public BrokerCfg()
    {
        super();
    }
    
    public BrokerCfg(String url)
    {
        super();
        this.url = url;
    }

    @XmlAttribute(name = "url")
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}

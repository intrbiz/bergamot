package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;

@XmlType(name = "exchange")
@XmlRootElement(name = "exchange")
public class ExchangeCfg extends Configuration
{
    private String type = "topic";

    private boolean persistent = true;
    
    private String defaultRoutingKey = null;

    public ExchangeCfg()
    {
        super();
    }
    
    public ExchangeCfg(String name, String type, boolean persistent, String defaultRoutingKey)
    {
        super();
        this.setName(name);
        this.type = type;
        this.persistent = persistent;
        this.defaultRoutingKey = defaultRoutingKey;
    }
    
    public ExchangeCfg(String name, String type, boolean persistent)
    {
        super();
        this.setName(name);
        this.type = type;
        this.persistent = persistent;
    }

    @XmlAttribute(name = "type")
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @XmlAttribute(name = "persistent")
    public boolean isPersistent()
    {
        return persistent;
    }

    public void setPersistent(boolean persistent)
    {
        this.persistent = persistent;
    }

    @XmlAttribute(name = "default-routing-key")
    public String getDefaultRoutingKey()
    {
        return defaultRoutingKey;
    }

    public void setDefaultRoutingKey(String defaultRoutingKey)
    {
        this.defaultRoutingKey = defaultRoutingKey;
    }
    
    public Exchange asExchange()
    {
        return new Exchange(this.getName(), this.getType(), this.isPersistent());
    }
    
    public GenericKey asKey()
    {
        return this.getDefaultRoutingKey() == null ? null : new GenericKey(this.getDefaultRoutingKey());
    }
}

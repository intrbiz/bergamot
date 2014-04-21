package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.name.Queue;

@XmlType(name = "queue")
@XmlRootElement(name = "queue")
public class QueueCfg extends Configuration
{
    private boolean persistent = true;

    public QueueCfg()
    {
        super();
    }
    
    public QueueCfg(String name, boolean persistent)
    {
        super();
        this.setName(name);
        this.persistent = persistent;
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
    
    public Queue asQueue()
    {
        return new Queue(this.getName(), this.isPersistent());
    }
}

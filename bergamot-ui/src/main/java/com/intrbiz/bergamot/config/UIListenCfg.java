package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "listen")
@XmlRootElement(name = "listen")
public class UIListenCfg
{
    private int scgiPort = 8090;
    
    private int websocketPort = 8081;
    
    private int scgiWorkers = 50;
    
    public UIListenCfg()
    {
        super();
    }

    @XmlAttribute(name = "scgi-port")
    public int getScgiPort()
    {
        return scgiPort;
    }

    public void setScgiPort(int scgiPort)
    {
        this.scgiPort = scgiPort;
    }

    @XmlAttribute(name = "websocket-port")
    public int getWebsocketPort()
    {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort)
    {
        this.websocketPort = websocketPort;
    }

    @XmlAttribute(name = "scgi-workers")
    public int getScgiWorkers()
    {
        return scgiWorkers;
    }

    public void setScgiWorkers(int scgiWorkers)
    {
        this.scgiWorkers = scgiWorkers;
    }
    
    
}

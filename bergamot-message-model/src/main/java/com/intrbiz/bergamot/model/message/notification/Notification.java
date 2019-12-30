package com.intrbiz.bergamot.model.message.notification;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.SiteMO;

/**
 * A notification
 */
public abstract class Notification extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("raised")
    private long raised;
    
    @JsonProperty("engine")
    private String engine;

    @JsonProperty("to")
    private List<ContactMO> to = new LinkedList<ContactMO>();
    
    @JsonProperty("site")
    private SiteMO site;

    public Notification()
    {
        super();
    }
    
    public <T extends Notification> T cloneNotification(String engine)
    {
        T notification = this.cloneMessge();
        notification.setEngine(engine);
        return notification;
    }
    
    @JsonIgnore
    public abstract String getNotificationType();

    public long getRaised()
    {
        return raised;
    }

    public void setRaised(long raised)
    {
        this.raised = raised;
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public List<ContactMO> getTo()
    {
        return to;
    }

    public void setTo(List<ContactMO> to)
    {
        this.to = to;
    }

    public SiteMO getSite()
    {
        return site;
    }

    public void setSite(SiteMO site)
    {
        this.site = site;
    }
}

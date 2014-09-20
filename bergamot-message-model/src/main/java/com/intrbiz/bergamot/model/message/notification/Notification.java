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
    @JsonProperty("raised")
    private long raised;

    @JsonProperty("to")
    private List<ContactMO> to = new LinkedList<ContactMO>();
    
    @JsonProperty("site")
    private SiteMO site;

    public Notification()
    {
        super();
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

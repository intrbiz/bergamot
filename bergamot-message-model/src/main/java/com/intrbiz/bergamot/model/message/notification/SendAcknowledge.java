package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ContactMO;

/**
 * An acknoweledge of an alert
 */
@JsonTypeName("bergamot.send_acknowledge")
public class SendAcknowledge extends CheckNotification
{
    @JsonProperty("acknowledged_by")
    private ContactMO acknowledgedBy;
    
    @JsonProperty("acknowledge_summary")
    private String acknowledgeSummary;
    
    @JsonProperty("acknowledge_comment")
    private String acknowledgeComment;
    
    public SendAcknowledge()
    {
        super();
    }
    
    public String getNotificationType()
    {
        return "acknowledge";
    }

    public ContactMO getAcknowledgedBy()
    {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(ContactMO acknowledgedBy)
    {
        this.acknowledgedBy = acknowledgedBy;
    }

    public String getAcknowledgeSummary()
    {
        return acknowledgeSummary;
    }

    public void setAcknowledgeSummary(String acknowledgeSummary)
    {
        this.acknowledgeSummary = acknowledgeSummary;
    }

    public String getAcknowledgeComment()
    {
        return acknowledgeComment;
    }

    public void setAcknowledgeComment(String acknowledgeComment)
    {
        this.acknowledgeComment = acknowledgeComment;
    }
}

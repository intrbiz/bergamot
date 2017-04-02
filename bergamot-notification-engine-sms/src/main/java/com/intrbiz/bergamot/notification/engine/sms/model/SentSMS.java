package com.intrbiz.bergamot.notification.engine.sms.model;

public class SentSMS
{    
    private boolean success;

    private String messageId;

    public SentSMS()
    {
        super();
    }

    public SentSMS(boolean success, String messageId)
    {
        super();
        this.success = success;
        this.messageId = messageId;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }
    
    public String toString()
    {
        return "Success: " + this.success + ", MessageId: " + this.messageId;
    }
}

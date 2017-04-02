package com.intrbiz.bergamot.notification.engine.sms.io;

public class SMSTransportException extends Exception
{
    private static final long serialVersionUID = 1L;

    public SMSTransportException()
    {
        super();
    }

    public SMSTransportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SMSTransportException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SMSTransportException(String message)
    {
        super(message);
    }

    public SMSTransportException(Throwable cause)
    {
        super(cause);
    }
}

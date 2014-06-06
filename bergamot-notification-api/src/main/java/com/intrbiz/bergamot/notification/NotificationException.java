package com.intrbiz.bergamot.notification;

public class NotificationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public NotificationException()
    {
        super();
    }

    public NotificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NotificationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotificationException(String message)
    {
        super(message);
    }

    public NotificationException(Throwable cause)
    {
        super(cause);
    }
}

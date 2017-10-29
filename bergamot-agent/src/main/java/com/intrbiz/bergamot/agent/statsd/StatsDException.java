package com.intrbiz.bergamot.agent.statsd;

public class StatsDException extends Exception
{
    private static final long serialVersionUID = 1L;

    public StatsDException()
    {
        super();
    }

    public StatsDException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public StatsDException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public StatsDException(String message)
    {
        super(message);
    }

    public StatsDException(Throwable cause)
    {
        super(cause);
    }
}

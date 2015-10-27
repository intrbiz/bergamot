package com.intrbiz.bergamot.check.jmx;

public class JMXException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public JMXException()
    {
        super();
    }

    public JMXException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public JMXException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JMXException(String message)
    {
        super(message);
    }

    public JMXException(Throwable cause)
    {
        super(cause);
    }
}

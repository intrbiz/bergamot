package com.intrbiz.bergamot;

public class BergamotAPIException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public BergamotAPIException()
    {
        super();
    }

    public BergamotAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BergamotAPIException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BergamotAPIException(String message)
    {
        super(message);
    }

    public BergamotAPIException(Throwable cause)
    {
        super(cause);
    }
}

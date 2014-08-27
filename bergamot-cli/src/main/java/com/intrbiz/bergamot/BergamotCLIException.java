package com.intrbiz.bergamot;

public class BergamotCLIException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public BergamotCLIException()
    {
        super();
    }

    public BergamotCLIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BergamotCLIException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BergamotCLIException(String message)
    {
        super(message);
    }

    public BergamotCLIException(Throwable cause)
    {
        super(cause);
    }

}

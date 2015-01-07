package com.intrbiz.bergamot.ui.security.password.check;

public class BadPassword extends Exception
{
    private static final long serialVersionUID = 1L;

    public BadPassword()
    {
        super();
    }

    public BadPassword(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BadPassword(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadPassword(String message)
    {
        super(message);
    }

    public BadPassword(Throwable cause)
    {
        super(cause);
    }
}

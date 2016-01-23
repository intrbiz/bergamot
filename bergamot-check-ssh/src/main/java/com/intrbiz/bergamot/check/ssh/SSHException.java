package com.intrbiz.bergamot.check.ssh;

public class SSHException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public SSHException()
    {
        super();
    }

    public SSHException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SSHException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SSHException(String message)
    {
        super(message);
    }

    public SSHException(Throwable cause)
    {
        super(cause);
    }
}

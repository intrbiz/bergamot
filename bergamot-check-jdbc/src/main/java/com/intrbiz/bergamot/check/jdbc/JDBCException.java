package com.intrbiz.bergamot.check.jdbc;

public class JDBCException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public JDBCException()
    {
        super();
    }

    public JDBCException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public JDBCException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JDBCException(String message)
    {
        super(message);
    }

    public JDBCException(Throwable cause)
    {
        super(cause);
    }
}

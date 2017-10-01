package com.intrbiz.bergamot.worker.engine.script;

public class BergamotScriptedCheckException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public BergamotScriptedCheckException()
    {
        super();
    }

    public BergamotScriptedCheckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BergamotScriptedCheckException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BergamotScriptedCheckException(String message)
    {
        super(message);
    }

    public BergamotScriptedCheckException(Throwable cause)
    {
        super(cause);
    }
}

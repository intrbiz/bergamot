package com.intrbiz.bergamot.model;

/**
 * Detailed status of a check
 */
public enum Status
{ 
    /**
     * The check is currently pending
     */
    PENDING,
    /**
     * The check is all ok, yay!
     */
    OK, 
    /**
     * The check has raised a warning, you may look into it
     */
    WARNING, 
    /**
     * The check has raised a critical error, you should look into it
     */
    CRITICAL, 
    /**
     * Something went wrong with the check
     */
    UNKNOWN,
    /**
     * The check did not execute quick enough and was cancelled
     */
    TIMEOUT,
    /**
     * Some Bergamot internal error happened, go look at the logs
     */
    INTERNAL;
    
    private Status()
    {
    }
    
    public int getCode()
    {
        return this.ordinal();
    }
    
    public static Status valueOf(int code)
    {
        for (Status status : Status.values())
        {
            if (status.getCode() == code) return status;
        }
        return null;
    }
}
package com.intrbiz.bergamot.model;

import org.apache.log4j.Logger;

/**
 * Detailed status of a check
 */
public enum Status
{ 
    /**
     * The check is currently pending
     */
    PENDING(true),
    /**
     * The check is all ok, yay!
     */
    OK(true), 
    /**
     * The check has raised a warning, you may look into it
     */
    WARNING(false), 
    /**
     * The check has raised a critical error, you should look into it
     */
    CRITICAL(false), 
    /**
     * Something went wrong with the check
     */
    UNKNOWN(false),
    /**
     * The check did not execute quick enough and was cancelled
     */
    TIMEOUT(false),
    /**
     * Some Bergamot internal error happened, go look at the logs
     */
    ERROR(false);
    
    private final boolean ok;
    
    private Status(boolean ok)
    {
        this.ok = ok;
    }
    
    public boolean isOk()
    {
        return this.ok;
    }
    
    public int getCode()
    {
        return this.ordinal();
    }
    
    public boolean isBetterThan(Status other)
    {
        return this.compareTo(other) < 0;
    }
    
    public boolean isWorseThan(Status other)
    {
        return this.compareTo(other) > 0;
    }
    
    public static Status valueOf(int code)
    {
        for (Status status : Status.values())
        {
            if (status.getCode() == code) return status;
        }
        return null;
    }
    
    public static Status best(Status a, Status b)
    {
        return a.compareTo(b) < 0 ? a : b;
    }
    
    public static Status worst(Status a, Status b)
    {
        return a.compareTo(b) > 0 ? a : b;
    }
    
    public static Status parse(String value)
    {
        try
        {
            return Status.valueOf(value.toUpperCase());
        }
        catch (Exception e)
        {
            Logger.getLogger(Status.class).warn("Failed to parse status: " + value, e);
        }
        return ERROR;
    }
}
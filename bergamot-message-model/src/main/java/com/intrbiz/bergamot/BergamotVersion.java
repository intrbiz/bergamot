package com.intrbiz.bergamot;

public final class BergamotVersion
{
    public static final String NAME = "Bergamot Monitoring";
    
    public static final int[] NUMBER = {4, 0, 0};
    
    public static final String CODE_NAME = "Red Beard";
    
    public static final String numberString()
    {
        return NUMBER[0] + "." + NUMBER[1] + "." + NUMBER[2];
    }
    
    public static final String versionString()
    {
        return NAME + " " + numberString();
    }
    
    public static final String fullVersionString()
    {
        return NAME + " " + numberString() + " (" + CODE_NAME + ")";
    }
}
package com.intrbiz.bergamot.util;

import java.security.SecureRandom;

/**
 * Generate a random nagios plugin exit code, for use in testing
 */
public class RandStatus
{
    private static final RandStatus US = new RandStatus();
    
    public static final RandStatus getInstance()
    {
        return US;
    }
    
    private SecureRandom random = new SecureRandom();
    
    public RandStatus()
    {
        super();
    }
    
    public int randomNagiosStatus()
    {
        int next = Math.abs(random.nextInt(100));
        if (next >=   0 && next < 60) return 0;
        if (next >= 60 && next < 80) return 1;
        if (next >= 80 && next < 90) return 2;
        return 3;
    }
}

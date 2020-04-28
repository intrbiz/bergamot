package com.intrbiz.bergamot.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.intrbiz.Util;

public class HostUtil
{
    private static final Logger logger = Logger.getLogger(HostUtil.class);

    public static final String getHostName()
    {
        return Util.coalesceEmpty(
                System.getenv("BERGAMOT_HOSTNAME"), 
                System.getProperty("bergamot.host.name"), 
                getLocalhostName()
        );
    }
    
    public static final String getLocalhostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            logger.warn("Failed to get localhost name", e);
        }
        return null;
    }
    
}

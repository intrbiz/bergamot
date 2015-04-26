package com.intrbiz.bergamot.agent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.SSLEngine;

public class TLSUtils
{
    public static final class PROTOCOLS
    {
        public static final String SSLv3 = "SSLv3";
        
        public static final String TLSv1 = "TLSv1";
        
        public static final String TLSv1_1 = "TLSv1.1";
        
        public static final String TLSv1_2 = "TLSv1.2";
        
        public static final String[] SAFE_PROTOCOLS = { TLSv1, TLSv1_1, TLSv1_2 };
        
        public static final String[] ALL_PROTOCOLS  = { SSLv3, TLSv1, TLSv1_1, TLSv1_2 };
    }
    
    public static String[] computeSupportedProtocols(SSLEngine sslEngine, String[] wantedProtocols)
    {
        Set<String> supported = new TreeSet<String>(Arrays.asList(sslEngine.getSupportedProtocols()));
        // filter the wanted protocols with that is supported
        List<String> protocols = new LinkedList<String>();
        for (String wanted : wantedProtocols)
        {
            if (supported.contains(wanted))
            {
                protocols.add(wanted);
            }
        }
        return protocols.toArray(new String[0]);
    }
            
            
}

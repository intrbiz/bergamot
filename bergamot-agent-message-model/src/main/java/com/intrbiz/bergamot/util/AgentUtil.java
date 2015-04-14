package com.intrbiz.bergamot.util;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

public class AgentUtil
{
    public static final String newNonce()
    {
        byte[] nonce = new byte[32];
        new SecureRandom().nextBytes(nonce);
        return Base64.encodeBase64URLSafeString(nonce);
    }
    
    /**
     * Is the string empty: is it null or ""
     */
    public final static boolean isEmpty(String s)
    {
        return (s == null || s.length() <= 0);
    }
}

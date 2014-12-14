package com.intrbiz.bergamot.updater.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.intrbiz.balsa.util.CookieSet;
import com.intrbiz.balsa.util.CookiesParser;

public class CookieJar implements CookieSet
{
    private final ConcurrentMap<String, String> cookies = new ConcurrentHashMap<String, String>();
    
    public CookieJar()
    {
        super();
    }

    @Override
    public String cookie(String name)
    {
        return this.cookies.get(name);
    }

    @Override
    public void cookie(String name, String value)
    {
        this.cookies.put(name, value);
    }

    @Override
    public Map<String, String> cookies()
    {
        return this.cookies;
    }

    @Override
    public Set<String> cookieNames()
    {
        return this.cookies.keySet();
    }

    @Override
    public void removeCookie(String name)
    {
        this.cookies.remove(name);
    }

    public static CookieJar parseCookies(String headerValue)
    {
        CookieJar jar = new CookieJar();
        if (headerValue != null)
            CookiesParser.parseCookies(headerValue, jar);
        return jar;
    }
}

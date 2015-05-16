package com.intrbiz.bergamot.check.http;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.check.http.HTTPChecker;

public class AuthTests
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        HTTPChecker hc = new HTTPChecker();
        
        hc.check().connect("10.250.100.144").port(15672).get("/api/overview").basicAuth("monitor", "monitor")
        .execute((r) -> { System.out.println(r.getResponse().content().toString(Util.UTF8)); }, (e) -> { e.printStackTrace(); });
        
    }
}

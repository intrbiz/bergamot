package com.intrbiz.bergamot.check.http;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.crypto.util.TLSConstants;

public class ProtocolTests
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        HTTPChecker hc = new HTTPChecker();
        //
        for (String protocol : TLSConstants.PROTOCOLS.ALL_PROTOCOLS)
        {
            hc.check()
            .connect("intrbiz.com")
            .ssl()
            .enabledSSLProtocols(protocol)
            .get("/")
            .execute((r) -> {
                System.out.println("Supported (intrbiz.com)  : " + protocol);
            }, (e) -> {
                System.out.println("Unsupported (intrbiz.com): " + protocol + " | " + e.getMessage());
            });
        }
    }
}

package com.intrbiz.bergamot.check.http;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.crypto.util.TLSConstants;

public class GCMTest
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        HTTPChecker hc = new HTTPChecker();
        
        hc.check().connect("sias.riskadvisory.net").https().enabledSSLCiphers(TLSConstants.CIPHERS.SAFE_CIPHERS).get("/index.php/auth")
        .execute((r) -> { System.out.println(r.getRuntime() + "ms\n" + r); }, (e) -> { e.printStackTrace(); });
        
    }
}

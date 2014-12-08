package com.intrbiz.bergamot.check.http;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.check.http.HTTPChecker;

public class SimpleTests
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        HTTPChecker hc = new HTTPChecker();
        
        hc.check().connect("www.bbc.co.uk").get("/")
        .execute((r) -> { System.out.println(r.getRuntime() + "ms\n" + r); }, (e) -> { e.printStackTrace(); });
        
        hc.check().connect("intrbiz.com").https().get("/")
        .execute((r) -> { System.out.println(r.getRuntime() + "ms\n" + r); }, (e) -> { e.printStackTrace(); });
        
        hc.check().connect("payments.sstaffs.gov.uk").https().get("/")
        .execute((r) -> { System.out.println(r.getRuntime() + "ms\n" + r); }, (e) -> { e.printStackTrace(); });
        
        hc.check().connect("obs.intrbiz.net").https().permitInvalidCerts().get("/")
        .execute((r) -> { System.out.println(r.getRuntime() + "ms\n" + r); }, (e) -> { e.printStackTrace(); });
        
        hc.check().connect("forms.shropshire.gov.uk").https().permitInvalidCerts().get("/")
        .execute((r) -> { System.out.println(r.getRuntime() + "ms\n" + r); }, (e) -> { e.printStackTrace(); });
        
        hc.check().connect("sias.riskadvisory.net").https().enabledSSLCiphers(TLSConstants.CIPHERS.SAFE_CIPHERS).get("/index.php/auth")
        .execute((r) -> { System.out.println(r.getRuntime() + "ms\n" + r); }, (e) -> { e.printStackTrace(); });
    }
}

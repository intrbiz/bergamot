package com.intrbiz.bergamot.check.http;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.crypto.util.TLSConstants;
import com.intrbiz.bergamot.crypto.util.TLSConstants.CipherInfo;

public class CipherTests
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        HTTPChecker hc = new HTTPChecker();
        //
        for (CipherInfo cipher : TLSConstants.CIPHERS.ALL_CIPHERS)
        {
            hc.check()
            .connect("intrbiz.com")
            .ssl()
            .enableSSLCipher(cipher.getName())
            .get("/")
            .execute((r) -> {
                System.out.println("Supported  : " + cipher);
            }, (e) -> {
                System.out.println("Unsupported: " + cipher + " | " + e.getMessage());
            });
        }
    }
}

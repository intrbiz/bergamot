package com.intrbiz.bergamot.worker.engine.http;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.worker.check.http.HTTPChecker;

public class BadSites
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        HTTPChecker hc = new HTTPChecker();
        //
        hc.check()
        .connect("onlinebanking.nationwide.co.uk")
        .ssl()
        .get("/")
        .execute((r) -> {
            System.out.println("Connected: " + r.getTlsInfo());
        }, (e) -> {
            e.printStackTrace();
        });
        //
        hc.check()
        .connect("oneonline.shropshire.gov.uk")
        .ssl()
        .get("/")
        .execute((r) -> {
            System.out.println("Connected: " + r.getTlsInfo());
        }, (e) -> {
            e.printStackTrace();
        });
    }
}

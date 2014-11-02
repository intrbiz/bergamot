package com.intrbiz.bergamot.pinger.example;

import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.pinger.Pinger;

public class PingerExample
{
    public static void main(String[] args) throws Exception
    {
        Pinger pinger = new Pinger();
        pinger.start();
        // add some targets
        pinger.addTarget("google.com",     15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
        pinger.addTarget("bbc.co.uk",      15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
        pinger.addTarget("intrbiz.com",    15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
        pinger.addTarget("172.30.14.1",    15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
        pinger.addTarget("172.30.13.42",   15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
        pinger.addTarget("172.30.13.242",  15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
        pinger.addTarget("10.250.100.242", 15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
        pinger.addTarget("some.bad.name",  15, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, (target, snapshot) -> { System.out.println(target.getHost() + " (" + target.getAddress() + ") " + snapshot); });
    }
}

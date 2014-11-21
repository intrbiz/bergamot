package com.intrbiz.bergamot.check.tcp;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SimpleConnectionTest
{
    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        //
        TCPChecker checker = new TCPChecker();
        checker.check()
        .connect("127.0.0.1", 22)
        .execute((r) -> {
            System.out.println(r);
        }, (e) -> {
            e.printStackTrace();
        });
    }
}

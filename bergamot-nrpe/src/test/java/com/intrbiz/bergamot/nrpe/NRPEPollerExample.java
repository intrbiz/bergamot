package com.intrbiz.bergamot.nrpe;



public class NRPEPollerExample
{
    public static void main(String[] args) throws Exception
    {
        NRPEPoller poller = new NRPEPoller();
        // execute some commands
        poller.command("127.0.0.1", 5666, 5, 10, (r) -> {System.out.println("Got " + r);}, (e) -> {System.out.println("Error: " + e);}, "check_mem");
        poller.command("172.30.13.30", 5666, 5, 10, (r) -> {System.out.println("Got " + r);}, (e) -> {System.out.println("Error: " + e);}, "check_disk_root");
        poller.command("172.30.13.30", 5666, 5, 10, (r) -> {System.out.println("Got " + r);}, (e) -> {System.out.println("Error: " + e);}, "check_mem");
        poller.command("127.0.0.1", 5666, 5, 10, (r) -> {System.out.println("Got " + r);}, (e) -> {System.out.println("Error: " + e);}, "blah_blah");
        // poller.command("127.0.0.1",    5666, 5, 60, null, (r, c) -> {System.out.println("Got " + r);}, (e, c) -> {System.out.println("Error: " + e);}, "check_disk_root");
        // randomly connect to loads of hosts
        /*
        for (int i = 10; i < 15; i++)
        {
            for (int j = 2; j < 250; j++)
            {
                poller.command("172.30." + i + "." + j, 5666, 5, 60, null, (r, c) -> {System.out.println("Got " + r);}, (e, c) -> {System.out.println("Error: " + e);}, "check_disk_root");
            }
        }
        */
    }
}

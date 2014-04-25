package com.intrbiz.bergamot.nrpe;

import java.io.IOException;

public class NRPEExample
{
    public static void main(String[] args)
    {
        try (NRPEClient client = new NRPEClient("127.0.0.1"))
        {
            System.out.println(client.command("check_disk_root"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

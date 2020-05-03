package com.intrbiz.bergamot.nrpe;

import java.io.IOException;

public class NRPEExample
{
    public static void main(String[] args)
    {
        try (NRPEClient client = new NRPEClient("172.30.4.21"))
        {
            System.out.println(client.command("check_load"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

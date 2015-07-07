package com.intrbiz.bergamot.nrpe;

import java.io.IOException;

public class NRPEArgsExample
{
    public static void main(String[] args)
    {
        try (NRPEClient client = new NRPEClient("127.0.0.1"))
        {
            System.out.println(client.command("check_disk_args", "20%", "10%", "/"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

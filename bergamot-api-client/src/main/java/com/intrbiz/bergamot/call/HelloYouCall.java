package com.intrbiz.bergamot.call;

import java.io.IOException;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;

public class HelloYouCall extends BergamotAPICall<String>
{    
    public HelloYouCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public String execute()
    {
        try
        {
            Response response = execute(get(url("/api/test/hello/you")).addHeader(authHeader()));
            return transcoder().decodeFromString(response.returnContent().asString(), String.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error calling hello you", e);
        }
    }
}

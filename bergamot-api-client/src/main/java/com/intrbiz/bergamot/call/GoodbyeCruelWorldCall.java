package com.intrbiz.bergamot.call;

import java.io.IOException;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;

public class GoodbyeCruelWorldCall extends BergamotAPICall<String>
{    
    public GoodbyeCruelWorldCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public String execute()
    {
        try
        {
            Response response = execute(get(url("/api/test/goodbye/cruel/world")).addHeader(authHeader()));
            return transcoder().decodeFromString(response.returnContent().asString(), String.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error calling goodbye cruel world", e);
        }
    }
}

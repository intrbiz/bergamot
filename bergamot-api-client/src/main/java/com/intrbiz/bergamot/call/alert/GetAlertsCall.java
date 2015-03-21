package com.intrbiz.bergamot.call.alert;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.AlertMO;

public class GetAlertsCall extends BergamotAPICall<List<AlertMO>>
{    
    public GetAlertsCall(BergamotClient client)
    {
        super(client);
    }
    
    public List<AlertMO> execute()
    {
        try
        {
            Response response = execute(get(url("/alert/")).addHeader(authHeader()));
            return transcoder().decodeListFromString(response.returnContent().asString(), AlertMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting alerts", e);
        }
    }
}

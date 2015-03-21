package com.intrbiz.bergamot.call.contact;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.ContactMO;

public class GetContactsCall extends BergamotAPICall<List<ContactMO>>
{    
    public GetContactsCall(BergamotClient client)
    {
        super(client);
    }
    
    public List<ContactMO> execute()
    {
        try
        {
            Response response = execute(get(url("/contact/")).addHeader(authHeader()));
            return transcoder().decodeListFromString(response.returnContent().asString(), ContactMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting contacts", e);
        }
    }
}

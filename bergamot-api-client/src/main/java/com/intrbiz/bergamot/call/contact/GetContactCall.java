package com.intrbiz.bergamot.call.contact;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.ContactMO;

public class GetContactCall extends BergamotAPICall<ContactMO>
{   
    private UUID id;
    
    public GetContactCall(BergamotClient client)
    {
        super(client);
    }
    
    public GetContactCall id(UUID id)
    {
        this.id = id;
        return this;
    }
    
    public ContactMO execute()
    {
        try
        {
            Response response = Request.Get(url("/contact/id/", this.id.toString())).addHeader(authHeader()).execute();
            return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting contact", e);
        }
    }
}

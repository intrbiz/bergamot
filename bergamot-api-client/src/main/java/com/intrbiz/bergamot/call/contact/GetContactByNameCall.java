package com.intrbiz.bergamot.call.contact;

import java.io.IOException;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.ContactMO;

public class GetContactByNameCall extends BergamotAPICall<ContactMO>
{   
    private String name;
    
    public GetContactByNameCall(BergamotClient client)
    {
        super(client);
    }
    
    public GetContactByNameCall name(String name)
    {
        this.name = name;
        return this;
    }
    
    public ContactMO execute()
    {
        try
        {
            Response response = execute(get(url("/contact/name/", this.name)).addHeader(authHeader()));
            return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting contact", e);
        }
    }
}

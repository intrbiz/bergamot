package com.intrbiz.bergamot.call.contact;

import java.io.IOException;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.model.message.ContactMO;

public class GetContactByEmailCall extends BergamotAPICall<ContactMO>
{   
    private String email;
    
    public GetContactByEmailCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public GetContactByEmailCall email(String email)
    {
        this.email = email;
        return this;
    }
    
    public ContactMO execute()
    {
        try
        {
            Response response = execute(get(url("/contact/email/", this.email)).addHeader(authHeader()));
            return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting contact", e);
        }
    }
}

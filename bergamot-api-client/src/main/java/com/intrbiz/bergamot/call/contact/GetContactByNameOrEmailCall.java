package com.intrbiz.bergamot.call.contact;

import java.io.IOException;

import org.apache.http.client.fluent.Response;

import com.intrbiz.Util;
import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.model.message.ContactMO;

public class GetContactByNameOrEmailCall extends BergamotAPICall<ContactMO>
{   
    private String email;
    
    private String name;
    
    public GetContactByNameOrEmailCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public GetContactByNameOrEmailCall email(String email)
    {
        this.email = email;
        return this;
    }
    
    public GetContactByNameOrEmailCall name(String name)
    {
        this.name = name;
        return this;
    }
    
    public ContactMO execute()
    {
        try
        {
            Response response = execute(get(url("/contact/name-or-email/", Util.coalesceEmpty(this.name, this.email))).addHeader(authHeader()));
            return transcoder().decodeFromString(response.returnContent().asString(), ContactMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting contact", e);
        }
    }
}

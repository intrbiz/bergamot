package com.intrbiz.bergamot.call.contact;

import java.io.IOException;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.config.model.ContactCfg;

public class GetContactConfigByNameCall extends BergamotAPICall<ContactCfg>
{   
    private String name;
    
    public GetContactConfigByNameCall(BergamotClient client)
    {
        super(client);
    }
    
    public GetContactConfigByNameCall name(String name)
    {
        this.name = name;
        return this;
    }
    
    public ContactCfg execute()
    {
        try
        {
            Response response = execute(get(url("/contact/name/", this.name, "/config.xml")).addHeader(authHeader()));
            return ContactCfg.fromString(ContactCfg.class, response.returnContent().asString());
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting contact config", e);
        }
    }
}

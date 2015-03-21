package com.intrbiz.bergamot.call.contact;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.config.model.ContactCfg;

public class GetContactConfigCall extends BergamotAPICall<ContactCfg>
{   
    private UUID id;
    
    public GetContactConfigCall(BergamotClient client)
    {
        super(client);
    }
    
    public GetContactConfigCall id(UUID id)
    {
        this.id = id;
        return this;
    }
    
    public ContactCfg execute()
    {
        try
        {
            Response response = execute(get(url("/contact/id/", this.id.toString(), "/config.xml")).addHeader(authHeader()));
            return ContactCfg.fromString(ContactCfg.class, response.returnContent().asString());
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting contact config", e);
        }
    }
}

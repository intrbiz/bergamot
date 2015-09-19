package com.intrbiz.bergamot.call.auth;

import java.io.IOException;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public class ExtendAuthTokenCall extends BergamotAPICall<AuthTokenMO>
{
    private String token;
    
    public ExtendAuthTokenCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public ExtendAuthTokenCall token(String token)
    {
        this.token = token;
        return this;
    }
    
    public AuthTokenMO execute()
    {
        try
        {
            Response response = execute(post(url("/extend-auth-token"))
                                 .bodyForm(Form.form().add("auth-token", this.token).build())
                                );
            return transcoder().decodeFromString(response.returnContent().asString(), AuthTokenMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error extending authentication token", e);
        }
    }
}

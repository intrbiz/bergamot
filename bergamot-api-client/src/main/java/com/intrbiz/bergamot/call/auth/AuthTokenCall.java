package com.intrbiz.bergamot.call.auth;

import java.io.IOException;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BaseBergamotClient;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public class AuthTokenCall extends BergamotAPICall<AuthTokenMO>
{
    private String username;
    
    private String password;
    
    public AuthTokenCall(BaseBergamotClient client)
    {
        super(client);
    }
    
    public AuthTokenCall username(String username)
    {
        this.username = username;
        return this;
    }
    
    public AuthTokenCall password(String password)
    {
        this.password = password;
        return this;
    }
    
    public AuthTokenMO execute()
    {
        try
        {
            Response response = execute(post(url("/auth-token"))
                                 .bodyForm(Form.form().add("username", this.username).add("password", this.password).build())
                                );
            return transcoder().decodeFromString(response.returnContent().asString(), AuthTokenMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting authentication token", e);
        }
    }
}

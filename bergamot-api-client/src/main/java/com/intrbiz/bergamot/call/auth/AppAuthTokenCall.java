package com.intrbiz.bergamot.call.auth;

import java.io.IOException;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Response;

import com.intrbiz.bergamot.BergamotAPICall;
import com.intrbiz.bergamot.BergamotAPIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public class AppAuthTokenCall extends BergamotAPICall<AuthTokenMO>
{
    private String app;
    
    private String username;
    
    private String password;
    
    public AppAuthTokenCall(BergamotClient client)
    {
        super(client);
    }
    
    public AppAuthTokenCall app(String app)
    {
        this.app = app;
        return this;
    }
    
    public AppAuthTokenCall username(String username)
    {
        this.username = username;
        return this;
    }
    
    public AppAuthTokenCall password(String password)
    {
        this.password = password;
        return this;
    }
    
    public AuthTokenMO execute()
    {
        try
        {
            Response response = execute(post(url("/app/auth-token"))
                                 .bodyForm(Form.form().add("app", this.app).add("username", this.username).add("password", this.password).build())
                                );
            return transcoder().decodeFromString(response.returnContent().asString(), AuthTokenMO.class);
        }
        catch (IOException e)
        {
            throw new BergamotAPIException("Error getting authentication token", e);
        }
    }
}

package com.intrbiz.bergamot.credentials;

import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public class BasicCredentials implements ClientCredentials
{
    private String username;
    
    private String password;
    
    public BasicCredentials(String username, String password)
    {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername()
    {
        return this.username;
    }
    
    public String getPassword()
    {
        return this.password;
    }

    @Override
    public AuthTokenMO auth(BergamotClient client)
    {
        return client.authToken().username(this.getUsername()).password(this.getPassword()).execute();
    }
}

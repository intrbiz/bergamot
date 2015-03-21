package com.intrbiz.bergamot.credentials;

import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.model.message.AuthTokenMO;

public class TokenCredentials implements ClientCredentials
{
    private String authToken;
    
    public TokenCredentials(String authToken)
    {
        this.authToken = authToken;
    }
    
    public String getAuthToken()
    {
        return this.authToken;
    }

    @Override
    public AuthTokenMO auth(BergamotClient client)
    {
        return new AuthTokenMO(this.getAuthToken(), -1);
    }
}

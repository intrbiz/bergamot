package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.auth_token")
public class AuthTokenMO extends MessageObject
{
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("expires_at")
    private long expiresAt;
    
    public AuthTokenMO()
    {
        super();
    }
    
    public AuthTokenMO(String token, long expiresAt)
    {
        super();
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public long getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt)
    {
        this.expiresAt = expiresAt;
    }
}

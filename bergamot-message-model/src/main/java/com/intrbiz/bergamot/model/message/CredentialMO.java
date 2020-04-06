package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A check credential
 */
@JsonTypeName("bergamot.credential")
public class CredentialMO extends SecuredObjectMO
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("key_id")
    private String keyId;
    
    @JsonProperty("key_secret")
    private String keySecret;

    public CredentialMO()
    {
        super();
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getKeyId()
    {
        return keyId;
    }

    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
    }

    public String getKeySecret()
    {
        return keySecret;
    }

    public void setKeySecret(String keySecret)
    {
        this.keySecret = keySecret;
    }
}

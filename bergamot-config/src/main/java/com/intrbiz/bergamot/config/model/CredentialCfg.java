package com.intrbiz.bergamot.config.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "credential")
@XmlRootElement(name = "credential")
public class CredentialCfg extends SecuredObjectCfg<CredentialCfg>
{
    private static final long serialVersionUID = 1L;
    
    private String username;
    
    private String password;
    
    private String keyId;
    
    private String keySecret;

    public CredentialCfg()
    {
        super();
    }

    @XmlElement(name = "username")
    @ResolveWith(CoalesceEmptyString.class)
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @XmlElement(name = "password")
    @ResolveWith(CoalesceEmptyString.class)
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @XmlElement(name = "key-id")
    @ResolveWith(CoalesceEmptyString.class)
    public String getKeyId()
    {
        return keyId;
    }

    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
    }

    @XmlElement(name = "key-secret")
    @ResolveWith(CoalesceEmptyString.class)
    public String getKeySecret()
    {
        return keySecret;
    }

    public void setKeySecret(String keySecret)
    {
        this.keySecret = keySecret;
    }
    
    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}

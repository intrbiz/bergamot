package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.agent.config.Configuration;

@XmlType(name = "site")
@XmlRootElement(name = "site")
public class CLISiteCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private String url;

    private String authToken;

    public CLISiteCfg()
    {
        super();
    }

    public CLISiteCfg(String name, String url, String authToken)
    {
        super();
        this.setName(name);
        this.url = url;
        this.authToken = authToken;
    }

    @Override
    public void applyDefaults()
    {
    }

    @XmlAttribute(name = "url")
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @XmlAttribute(name = "auth-token")
    public String getAuthToken()
    {
        return authToken;
    }

    public void setAuthToken(String authToken)
    {
        this.authToken = authToken;
    }
}
